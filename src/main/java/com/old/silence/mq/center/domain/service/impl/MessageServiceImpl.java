package com.old.silence.mq.center.domain.service.impl;


import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.common.message.MessageClientIDSetter;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.body.Connection;
import org.apache.rocketmq.remoting.protocol.body.ConsumeMessageDirectlyResult;
import org.apache.rocketmq.remoting.protocol.body.ConsumerConnection;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.apache.rocketmq.tools.admin.api.MessageTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.old.silence.mq.center.api.config.RMQConfigure;
import com.old.silence.mq.center.domain.model.MessagePage;
import com.old.silence.mq.center.domain.model.MessagePageTask;
import com.old.silence.mq.center.domain.model.MessageQueryByPage;
import com.old.silence.mq.center.domain.model.MessageView;
import com.old.silence.mq.center.domain.model.QueueOffsetInfo;
import com.old.silence.mq.center.domain.model.request.MessageQuery;
import com.old.silence.mq.center.domain.service.MessageService;
import com.old.silence.mq.center.domain.service.facade.RocketMQClientFacade;
import com.old.silence.mq.center.domain.service.helper.MessageQueryHelper;
import com.old.silence.mq.center.domain.service.template.ConsumerTemplate;
import com.old.silence.mq.center.domain.service.template.MessagePullTemplate;
import com.old.silence.mq.center.exception.ServiceException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Cache<String, List<QueueOffsetInfo>> CACHE = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .build();
    /**
     * @see org.apache.rocketmq.store.config.MessageStoreConfig maxMsgsNumBatch = 64;
     * @see org.apache.rocketmq.store.index.IndexService maxNum = Math.min(maxNum, this.defaultMessageStore.getMessageStoreConfig().getMaxMsgsNumBatch());
     */
    private final static int QUERY_MESSAGE_MAX_NUM = 64;
    private final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);
    private final RMQConfigure configure;
    private final MQAdminExt mqAdminExt;

    private final RocketMQClientFacade mqFacade;

    public MessageServiceImpl(RMQConfigure configure, MQAdminExt mqAdminExt, RocketMQClientFacade mqFacade) {
        this.configure = configure;
        this.mqAdminExt = mqAdminExt;
        this.mqFacade = mqFacade;
    }

    @Override
    public Pair<MessageView, List<MessageTrack>> viewMessage(String subject, final String msgId) {
        try {
            MessageExt messageExt = mqFacade.viewMessage(subject, msgId);
            List<MessageTrack> messageTrackList = messageTrackDetail(messageExt);
            return new Pair<>(MessageView.fromMessageExt(messageExt), messageTrackList);
        } catch (Exception e) {
            throw new ServiceException(-1, String.format("Failed to query message by Id: %s", msgId));
        }
    }

    @Override
    public List<MessageView> queryMessageByTopicAndKey(String topic, String key) {
        try {
            return Lists.transform(mqFacade.queryMessage(topic, key), new Function<MessageExt, MessageView>() {
                @Override
                public MessageView apply(MessageExt messageExt) {
                    return MessageView.fromMessageExt(messageExt);
                }
            });
        } catch (Exception err) {
            if (err instanceof MQClientException) {
                throw new ServiceException(-1, ((MQClientException) err).getErrorMessage());
            }
            Throwables.throwIfUnchecked(err);
            throw new RuntimeException(err);
        }
    }

    @Override
    public List<MessageView> queryMessageByTopic(String topic, final long begin, final long end) {
        try {
            RPCHook rpcHook = ConsumerTemplate.createAclHook(configure.getAccessKey(), configure.getSecretKey());
            return ConsumerTemplate.executeWithConsumer(rpcHook, configure.isUseTLS(), consumer -> {
                List<MessageView> result = new java.util.ArrayList<>();

                for (org.apache.rocketmq.common.message.MessageQueue mq : consumer.fetchSubscribeMessageQueues(topic)) {
                    long minOffset = consumer.searchOffset(mq, begin);
                    long maxOffset = consumer.searchOffset(mq, end);

                    List<MessageView> messages = MessagePullTemplate.pullMessagesInTimeRange(
                            consumer, mq, minOffset, begin, end, 2000);
                    result.addAll(messages);

                    if (result.size() >= 2000) {
                        break;
                    }
                }

                // 排序结果
                result.sort((o1, o2) -> {
                    if (o1.getStoreTimestamp() == o2.getStoreTimestamp()) {
                        return 0;
                    }
                    return o1.getStoreTimestamp() > o2.getStoreTimestamp() ? -1 : 1;
                });

                return result;
            });
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<MessageTrack> messageTrackDetail(MessageExt msg) {
        try {
            return mqFacade.queryMessageTrack(msg).getTrackList();
        } catch (Exception e) {
            logger.error("op=messageTrackDetailError", e);
            return Collections.emptyList();
        }
    }


    @Override
    public ConsumeMessageDirectlyResult consumeMessageDirectly(String topic, String msgId, String consumerGroup,
                                                               String clientId) {
        if (StringUtils.isNotBlank(clientId)) {
            try {
                return mqFacade.consumeMessageDirectly(consumerGroup, clientId, topic, msgId);
            } catch (Exception e) {
                Throwables.throwIfUnchecked(e);
                throw new RuntimeException(e);
            }
        }

        try {
            ConsumerConnection consumerConnection = mqFacade.getConsumerConnection(consumerGroup);
            for (Connection connection : consumerConnection.getConnectionSet()) {
                if (StringUtils.isBlank(connection.getClientId())) {
                    continue;
                }
                logger.info("clientId={}", connection.getClientId());
                return mqFacade.consumeMessageDirectly(consumerGroup, connection.getClientId(), topic, msgId);
            }
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
        throw new IllegalStateException("NO CONSUMER");

    }

    @Override
    public MessagePage queryMessageByPage(MessageQuery query) {
        MessageQueryByPage queryByPage = new MessageQueryByPage(
                query.getPageNo(),
                query.getPageSize(),
                query.getTopic(),
                query.getBegin(),
                query.getEnd());

        List<QueueOffsetInfo> cachedOffsets = CACHE.getIfPresent(query.getTaskId());

        if (cachedOffsets == null) {
            // 首次查询，获取第一页
            MessagePageTask task = queryFirstMessagePage(queryByPage);
            String taskId = MessageClientIDSetter.createUniqID();
            CACHE.put(taskId, task.getQueueOffsetInfos());
            return new MessagePage(task.getPage(), taskId);
        }

        // 后续查询，使用缓存的offset
        Page<MessageView> messageViews = queryMessageByTaskPage(queryByPage, cachedOffsets);
        return new MessagePage(messageViews, query.getTaskId());
    }

    private MessagePageTask queryFirstMessagePage(MessageQueryByPage query) {
        try {
            RPCHook rpcHook = ConsumerTemplate.createAclHook(configure.getAccessKey(), configure.getSecretKey());

            return ConsumerTemplate.executeWithConsumer(rpcHook, configure.isUseTLS(), consumer -> {
                // 初始化队列offset信息
                List<QueueOffsetInfo> queueOffsets = MessageQueryHelper.initializeQueueOffsets(consumer, query);

                // 调整offset范围到查询时间范围
                MessageQueryHelper.adjustStartOffsets(consumer, queueOffsets, query);
                MessageQueryHelper.adjustEndOffsets(consumer, queueOffsets, query);

                // 计算总消息数和页大小
                long total = MessageQueryHelper.calculateTotalMessages(queueOffsets);
                long pageSize = Math.min(total, query.getPageSize());

                // 移动offset用于分页
                int next = MessageQueryHelper.moveStartOffset(queueOffsets, query);
                MessageQueryHelper.moveEndOffset(queueOffsets, query, next);

                // 拉取第一页的消息
                List<MessageView> messages = MessagePullTemplate.pullMessagesFromQueues(consumer, queueOffsets, pageSize);

                PageImpl<MessageView> page = new PageImpl<>(messages, query.page(), total);
                return new MessagePageTask(page, queueOffsets);
            });
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    private Page<MessageView> queryMessageByTaskPage(MessageQueryByPage query, List<QueueOffsetInfo> queueOffsets) {
        try {
            RPCHook rpcHook = ConsumerTemplate.createAclHook(configure.getAccessKey(), configure.getSecretKey());

            return ConsumerTemplate.executeWithConsumer(rpcHook, configure.isUseTLS(), consumer -> {
                // 重新初始化offset起始点
                for (QueueOffsetInfo info : queueOffsets) {
                    info.setStartOffset(info.getStart());
                    info.setEndOffset(info.getStart());
                }

                // 计算总消息数
                long total = MessageQueryHelper.calculateTotalMessages(queueOffsets);
                long offset = (long) query.getPageNo() * query.getPageSize();

                if (total <= offset) {
                    return Page.empty();
                }

                long pageSize = Math.min(total - offset, query.getPageSize());

                // 移动offset用于分页
                int next = MessageQueryHelper.moveStartOffset(queueOffsets, query);
                MessageQueryHelper.moveEndOffset(queueOffsets, query, next);

                // 拉取当前页的消息
                List<MessageView> messages = MessagePullTemplate.pullMessagesFromQueues(consumer, queueOffsets, pageSize);

                return new PageImpl<>(messages, query.page(), total);
            });
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    private int moveStartOffset(List<QueueOffsetInfo> queueOffsets, MessageQueryByPage query) {
        return MessageQueryHelper.moveStartOffset(queueOffsets, query);
    }

    private void moveEndOffset(List<QueueOffsetInfo> queueOffsets, MessageQueryByPage query, int next) {
        MessageQueryHelper.moveEndOffset(queueOffsets, query, next);
    }

    public DefaultMQPullConsumer buildDefaultMQPullConsumer(RPCHook rpcHook, boolean useTLS) {
        return ConsumerTemplate.createConsumer(rpcHook, useTLS);
    }
}
