

package com.old.silence.mq.center.domain.service.impl;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.remoting.protocol.body.ConsumeMessageDirectlyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.old.silence.mq.center.domain.model.DlqMessageRequest;
import com.old.silence.mq.center.domain.model.DlqMessageResendResult;
import com.old.silence.mq.center.domain.model.MessagePage;
import com.old.silence.mq.center.domain.model.MessageView;
import com.old.silence.mq.center.domain.model.request.MessageQuery;
import com.old.silence.mq.center.domain.service.DlqMessageService;
import com.old.silence.mq.center.domain.service.MessageService;
import com.old.silence.mq.center.domain.service.facade.RocketMQClientFacade;
import com.old.silence.mq.center.exception.ServiceException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


@Service
public class DlqMessageServiceImpl implements DlqMessageService {


    private static final Logger log = LoggerFactory.getLogger(DlqMessageServiceImpl.class);

    private final MessageService messageService;
    
    private final RocketMQClientFacade mqFacade;

    public DlqMessageServiceImpl(MessageService messageService, RocketMQClientFacade mqFacade) {
        this.messageService = messageService;
        this.mqFacade = mqFacade;
    }

    @Override
    public MessagePage queryDlqMessageByPage(MessageQuery query) {
        List<MessageView> messageViews = new ArrayList<>();
        PageRequest page = PageRequest.of(query.getPageNo(), query.getPageSize());
        String topic = query.getTopic();
        try {
            mqFacade.getTopicRoute(topic);
        } catch (ServiceException e) {
            // If the %DLQ%Group does not exist, the message returns null
            if (topic.startsWith(MixAll.DLQ_GROUP_TOPIC_PREFIX)
                && e.getMessage().contains("TOPIC_NOT_EXIST")) {
                return new MessagePage(new PageImpl<>(messageViews, page, 0), query.getTaskId());
            } else {
                throw e;
            }
        } catch (Exception e) {
            throw new ServiceException(-1, "Failed to query DLQ message: " + e.getMessage());
        }
        return messageService.queryMessageByPage(query);
    }

    @Override
    public List<DlqMessageResendResult> batchResendDlqMessage(List<DlqMessageRequest> dlqMessages) {
        List<DlqMessageResendResult> batchResendResults = new LinkedList<>();
        for (DlqMessageRequest dlqMessage : dlqMessages) {
            ConsumeMessageDirectlyResult result = messageService.consumeMessageDirectly(dlqMessage.getTopicName(),
                dlqMessage.getMsgId(), dlqMessage.getConsumerGroup(),
                dlqMessage.getClientId());
            DlqMessageResendResult resendResult = new DlqMessageResendResult(result, dlqMessage.getMsgId());
            batchResendResults.add(resendResult);
        }
        return batchResendResults;
    }
}
