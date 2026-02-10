package com.old.silence.mq.center.domain.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.topic.TopicValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.old.silence.mq.center.domain.model.MessageTraceView;
import com.old.silence.mq.center.domain.model.trace.MessageTraceGraph;
import com.old.silence.mq.center.domain.service.MessageTraceService;
import com.old.silence.mq.center.domain.service.facade.RocketMQClientFacade;
import com.old.silence.mq.center.domain.service.helper.TraceGraphBuilder;
import com.old.silence.mq.center.exception.ServiceException;

import java.util.ArrayList;
import java.util.List;


@Service
public class MessageTraceServiceImpl implements MessageTraceService {

    private final Logger logger = LoggerFactory.getLogger(MessageTraceServiceImpl.class);

    private final RocketMQClientFacade mqFacade;

    public MessageTraceServiceImpl(RocketMQClientFacade mqFacade) {
        this.mqFacade = mqFacade;
    }

    @Override
    public List<MessageTraceView> queryMessageTraceKey(String key) {
        String queryTopic = TopicValidator.RMQ_SYS_TRACE_TOPIC;
        logger.info("query data topic name is:{}", queryTopic);
        return queryMessageTraceByTopicAndKey(queryTopic, key);
    }

    @Override
    public List<MessageTraceView> queryMessageTraceByTopicAndKey(String topic, String key) {
        try {
            List<MessageTraceView> messageTraceViews = new ArrayList<MessageTraceView>();
            List<MessageExt> messageTraceList = mqFacade.queryMessage(topic, key);
            for (MessageExt messageExt : messageTraceList) {
                List<MessageTraceView> messageTraceView = MessageTraceView.decodeFromTraceTransData(key, messageExt);
                messageTraceViews.addAll(messageTraceView);
            }
            return messageTraceViews;
        } catch (Exception err) {
            throw new ServiceException(-1, String.format("Failed to query message trace by msgId %s", key));
        }
    }

    @Override
    public MessageTraceGraph queryMessageTraceGraph(String key, String topic) {
        if (StringUtils.isEmpty(topic)) {
            topic = TopicValidator.RMQ_SYS_TRACE_TOPIC;
        }
        List<MessageTraceView> messageTraceViews = queryMessageTraceByTopicAndKey(topic, key);
        return buildMessageTraceGraph(messageTraceViews);
    }

    private MessageTraceGraph buildMessageTraceGraph(List<MessageTraceView> messageTraceViews) {
        return TraceGraphBuilder.buildGraph(messageTraceViews);
    }
}
