package com.old.silence.mq.center.domain.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Throwables;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.mq.center.dto.DlqMessageRequest;
import com.old.silence.mq.center.dto.DlqMessageResendResult;
import com.old.silence.mq.center.dto.MessagePage;
import com.old.silence.mq.center.dto.MessageQuery;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.apache.rocketmq.remoting.protocol.ResponseCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author moryzang
 */
@Service
public class DlqMessageService {

    private final MQAdminService mqAdminService;
    private final MessageService messageService;
    private final MqProducerService mqProducerService;

    public DlqMessageService(MQAdminService mqAdminService, MessageService messageService,
                              MqProducerService mqProducerService) {
        this.mqAdminService = mqAdminService;
        this.messageService = messageService;
        this.mqProducerService = mqProducerService;
    }

    public MessagePage queryDlqMessageByPage(MessageQuery query) throws Exception {
        // Accept consumerGroup from dedicated field; fall back to topic for backward compat
        if (StringUtils.isBlank(query.getTopic())) {
            return new MessagePage(new Page<>(), null);
        }

        String topic = query.getTopic();

        // Validate DLQ topic exists; return empty page if not (consumer group never had DLQ)
        try {
            mqAdminService.executeVoid(admin -> {
                admin.examineTopicRouteInfo(topic);
            });
        } catch (MQClientException e) {
            if (topic.startsWith(MixAll.DLQ_GROUP_TOPIC_PREFIX)
                    && e.getResponseCode() == ResponseCode.TOPIC_NOT_EXIST) {
                return new MessagePage(new Page<>(), query.getTaskId());
            } else {
                Throwables.throwIfUnchecked(e);
                throw new RuntimeException(e);
            }
        }

        return messageService.queryMessageByPage(query);
    }

    public List<DlqMessageResendResult> batchResendDlqMessage(List<DlqMessageRequest> dlqMessages) {
        if (CollectionUtils.isEmpty(dlqMessages)) {
            return new ArrayList<>();
        }

        List<DlqMessageResendResult> results = new ArrayList<>();
        for (DlqMessageRequest req : dlqMessages) {
            try {
                // 1. 从 DLQ topic 查出原始消息
                String dlqTopic = MixAll.DLQ_GROUP_TOPIC_PREFIX + req.getConsumerGroup();
                MessageExt oldMsg = mqAdminService.execute(admin -> admin.viewMessage(dlqTopic, req.getMsgId()));

                // 2. 取原始业务 topic（DLQ 消息的 RETRY_TOPIC 属性即为原始 topic）
                var newMsg = getMessage(req, oldMsg);

                // 4. 通过 MqProducerService 发送
                SendResult sendResult = mqProducerService.send(newMsg);
                results.add(new DlqMessageResendResult(req.getMsgId(),
                        "SUCCESS, new msgId=" + sendResult.getMsgId()));
            } catch (Exception ex) {
                results.add(new DlqMessageResendResult(req.getMsgId(), "FAIL: " + ex.getMessage()));
            }
        }
        return results;
    }

    @NotNull
    private static Message getMessage(DlqMessageRequest req, MessageExt oldMsg) {
        Map<String, String> props = oldMsg.getProperties();
        String retryTopic = props.getOrDefault("RETRY_TOPIC", req.getTopicName());

        // 3. 构造新消息，保留 tags / keys / 用户自定义属性
        Message newMsg = new Message(retryTopic, oldMsg.getTags(), oldMsg.getKeys(), oldMsg.getBody());
        props.forEach((k, v) -> {
            // 跳过 RocketMQ 内部系统属性，避免被重新消费时误判
            if (!k.startsWith("TRAN_") && !k.equals("DELAY") && !k.equals("WAIT")) {
                newMsg.putUserProperty(k, v);
            }
        });
        return newMsg;
    }
}
