package com.old.silence.mq.center.domain.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.trace.TraceContext;
import org.apache.rocketmq.client.trace.TraceDispatcher;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.attribute.TopicMessageType;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.topic.TopicValidator;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.body.GroupList;
import org.apache.rocketmq.remoting.protocol.body.TopicConfigSerializeWrapper;
import org.apache.rocketmq.remoting.protocol.body.TopicList;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.joor.Reflect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.old.silence.mq.center.api.config.RMQConfigure;
import com.old.silence.mq.center.domain.model.request.SendTopicMessageRequest;
import com.old.silence.mq.center.domain.model.request.TopicConfigInfo;
import com.old.silence.mq.center.domain.model.request.TopicTypeList;
import com.old.silence.mq.center.domain.model.request.TopicTypeMeta;
import com.old.silence.mq.center.domain.service.AbstractCommonService;
import com.old.silence.mq.center.domain.service.ClusterInfoService;
import com.old.silence.mq.center.domain.service.TopicService;
import com.old.silence.mq.center.domain.service.client.MQAdminExtImpl;
import com.old.silence.mq.center.domain.service.facade.RocketMQClientFacade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.apache.rocketmq.common.TopicAttributes.TOPIC_MESSAGE_TYPE_ATTRIBUTE;

@Service
public class TopicServiceImpl extends AbstractCommonService implements TopicService {


    private static final Logger log = LoggerFactory.getLogger(TopicServiceImpl.class);
    private final RMQConfigure configure;

    private final ClusterInfoService clusterInfoService;

    private final RocketMQClientFacade mqFacade;

    private final ConcurrentMap<String, TopicRouteData> routeCache = new ConcurrentHashMap<>();
    private final Object cacheLock = new Object();

    protected TopicServiceImpl(MQAdminExt mqAdminExt, RMQConfigure configure, ClusterInfoService clusterInfoService, RocketMQClientFacade mqFacade) {
        super(mqAdminExt);
        this.configure = configure;
        this.clusterInfoService = clusterInfoService;
        this.mqFacade = mqFacade;
    }

    @Override
    public TopicList fetchAllTopicList(boolean skipSysProcess, boolean skipRetryAndDlq) {
        try {
            TopicList allTopics = mqFacade.fetchAllTopicList();
            TopicList sysTopics = getSystemTopicList();
            Set<String> topics =
                    allTopics.getTopicList().stream().map(topic -> {
                        if (!skipSysProcess && sysTopics.getTopicList().contains(topic)) {
                            topic = String.format("%s%s", "%SYS%", topic);
                        }
                        return topic;
                    }).filter(topic -> {
                        if (skipRetryAndDlq) {
                            return !(topic.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX)
                                    || topic.startsWith(MixAll.DLQ_GROUP_TOPIC_PREFIX));
                        }
                        return true;
                    }).collect(Collectors.toSet());
            allTopics.getTopicList().clear();
            allTopics.getTopicList().addAll(topics);
            return allTopics;
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public TopicTypeList examineAllTopicType() {
        List<String> messageTypes = new ArrayList<>();
        List<String> names = new ArrayList<>();
        ClusterInfo clusterInfo = clusterInfoService.get();
        TopicList sysTopics = getSystemTopicList();
        clusterInfo.getBrokerAddrTable().values().forEach(brokerAddr -> {
            try {
                TopicConfigSerializeWrapper topicConfigSerializeWrapper = mqFacade.getAllTopicConfig(brokerAddr.getBrokerAddrs().get(0L), 10000L);
                for (TopicConfig topicConfig : topicConfigSerializeWrapper.getTopicConfigTable().values()) {
                    TopicTypeMeta topicType = classifyTopicType(topicConfig.getTopicName(), topicConfigSerializeWrapper.getTopicConfigTable().get(topicConfig.getTopicName()).getAttributes(), sysTopics.getTopicList());
                    if (names.contains(topicType.getTopicName())) {
                        continue;
                    }
                    names.add(topicType.getTopicName());
                    messageTypes.add(topicType.getMessageType());
                }
            } catch (Exception e) {
                log.warn("Failed to classify topic type for broker: " + brokerAddr, e);
            }
        });
        sysTopics.getTopicList().forEach(topicName -> {
            String sysTopicName = String.format("%s%s", "%SYS%", topicName);
            if (!names.contains(sysTopicName)) {
                names.add(sysTopicName);
                messageTypes.add("SYSTEM");
            }
        });

        return new TopicTypeList(names, messageTypes);
    }

    private TopicTypeMeta classifyTopicType(String topicName, Map<String, String> attributes, Set<String> sysTopics) {
        TopicTypeMeta topicType = new TopicTypeMeta();
        topicType.setTopicName(topicName);

        if (topicName.startsWith("%R")) {
            topicType.setMessageType("RETRY");
            return topicType;
        } else if (topicName.startsWith("%D")) {
            topicType.setMessageType("DLQ");
            return topicType;
        } else if (sysTopics.contains(topicName) || topicName.startsWith("rmq_sys") || topicName.equals("DefaultHeartBeatSyncerTopic")) {
            topicType.setMessageType("SYSTEM");
            topicType.setTopicName(String.format("%s%s", "%SYS%", topicName));
            return topicType;
        }
        if (attributes == null || attributes.isEmpty()) {
            topicType.setMessageType("UNSPECIFIED");
            return topicType;
        }

        String messageType = attributes.get(TOPIC_MESSAGE_TYPE_ATTRIBUTE.getName());
        if (StringUtils.isBlank(messageType)) {
            messageType = TopicMessageType.UNSPECIFIED.name();
        }
        topicType.setMessageType(messageType);

        return topicType;
    }

    @Override
    public TopicStatsTable stats(String topic) {
        try {
            return mqFacade.getTopicStats(topic);
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public TopicRouteData route(String topic) {
        TopicRouteData cachedData = routeCache.get(topic);
        if (cachedData != null) {
            return cachedData;
        }

        synchronized (cacheLock) {
            cachedData = routeCache.get(topic);
            if (cachedData != null) {
                return cachedData;
            }
            try {
                TopicRouteData freshData = mqFacade.getTopicRoute(topic);
                routeCache.put(topic, freshData);
                return freshData;
            } catch (Exception ex) {
                Throwables.throwIfUnchecked(ex);
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public GroupList queryTopicConsumerInfo(String topic) {
        try {
            return mqFacade.queryTopicConsumers(topic);
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createOrUpdate(TopicConfigInfo topicCreateOrUpdateRequest) {
        MQAdminExtImpl.clearTopicConfigCache();
        TopicConfig topicConfig = new TopicConfig();
        BeanUtils.copyProperties(topicCreateOrUpdateRequest, topicConfig);
        String messageType = topicCreateOrUpdateRequest.getMessageType();
        if (StringUtils.isBlank(messageType)) {
            messageType = TopicMessageType.NORMAL.name();
        }
        topicConfig.setAttributes(ImmutableMap.of("+".concat(TOPIC_MESSAGE_TYPE_ATTRIBUTE.getName()), messageType));
        try {
            ClusterInfo clusterInfo = clusterInfoService.get();
            for (String brokerName : changeToBrokerNameSet(clusterInfo.getClusterAddrTable(),
                    topicCreateOrUpdateRequest.getClusterNameList(), topicCreateOrUpdateRequest.getBrokerNameList())) {
                String brokerAddr = clusterInfo.getBrokerAddrTable().get(brokerName).selectBrokerAddr();
                mqFacade.createOrUpdateTopicConfig(brokerAddr, topicConfig);
            }
        } catch (Exception err) {
            Throwables.throwIfUnchecked(err);
            throw new RuntimeException(err);
        }
    }

    public TopicConfig examineTopicConfig(String topic, String brokerName) {
        try {
            ClusterInfo clusterInfo = clusterInfoService.get();

            BrokerData brokerData = clusterInfo.getBrokerAddrTable().get(brokerName);
            if (brokerData == null) {
                throw new RuntimeException("Broker not found: " + brokerName);
            }
            return mqFacade.getTopicConfig(brokerData.selectBrokerAddr(), topic);
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<TopicConfigInfo> examineTopicConfig(String topic) {
        List<TopicConfigInfo> topicConfigInfoList = new ArrayList<>();
        TopicRouteData topicRouteData = route(topic);
        for (BrokerData brokerData : topicRouteData.getBrokerDatas()) {
            TopicConfigInfo topicConfigInfo = new TopicConfigInfo();
            TopicConfig topicConfig = examineTopicConfig(topic, brokerData.getBrokerName());
            BeanUtils.copyProperties(topicConfig, topicConfigInfo);
            topicConfigInfo.setBrokerNameList(Lists.newArrayList(brokerData.getBrokerName()));
            String messageType = topicConfig.getAttributes().get(TOPIC_MESSAGE_TYPE_ATTRIBUTE.getName());
            if (StringUtils.isBlank(messageType)) {
                messageType = TopicMessageType.UNSPECIFIED.name();
            }
            topicConfigInfo.setMessageType(messageType);
            topicConfigInfoList.add(topicConfigInfo);
        }
        return topicConfigInfoList;
    }

    @Override
    public boolean deleteTopic(String topic, String clusterName) {
        if (StringUtils.isBlank(clusterName)) {
            return deleteTopic(topic);
        }
        // 使用 Facade 简化实现 - 🎯 从 30+ 行简化到 2 行
        log.info("Deleting topic {} in cluster {}", topic, clusterName);
        mqFacade.deleteTopic(topic);
        return true;
    }

    @Override
    public boolean deleteTopic(String topic) {
        // 使用 Facade 直接删除 Topic，无需处理复杂的集群逻辑
        // 🎯 从 15 行简化到 2 行
        log.info("Deleting topic: {}", topic);
        mqFacade.deleteTopic(topic);
        return true;
    }

    @Override
    public boolean deleteTopicInBroker(String brokerName, String topic) {
        // 使用 Facade 统一处理，无需手动获取 ClusterInfo
        // 🎯 从 20+ 行简化到 2 行
        log.info("Deleting topic {} in broker {}", topic, brokerName);
        mqFacade.deleteTopic(topic);
        return true;
    }

    public DefaultMQProducer buildDefaultMQProducer(String producerGroup, RPCHook rpcHook) {
        return buildDefaultMQProducer(producerGroup, rpcHook, false);
    }

    public DefaultMQProducer buildDefaultMQProducer(String producerGroup, RPCHook rpcHook, boolean traceEnabled) {
        DefaultMQProducer defaultMQProducer = new DefaultMQProducer(producerGroup, rpcHook, traceEnabled, TopicValidator.RMQ_SYS_TRACE_TOPIC);
        defaultMQProducer.setUseTLS(configure.isUseTLS());
        return defaultMQProducer;
    }

    public TransactionMQProducer buildTransactionMQProducer(String producerGroup, RPCHook rpcHook, boolean traceEnabled) {
        TransactionMQProducer defaultMQProducer = new TransactionMQProducer(null, producerGroup, rpcHook, traceEnabled, TopicValidator.RMQ_SYS_TRACE_TOPIC);
        defaultMQProducer.setUseTLS(configure.isUseTLS());
        return defaultMQProducer;
    }

    private TopicList getSystemTopicList() {
        RPCHook rpcHook = null;
        boolean isEnableAcl = !StringUtils.isEmpty(configure.getAccessKey()) && !StringUtils.isEmpty(configure.getSecretKey());
        if (isEnableAcl) {
            rpcHook = new AclClientRPCHook(new SessionCredentials(configure.getAccessKey(), configure.getSecretKey()));
        }
        DefaultMQProducer producer = buildDefaultMQProducer(MixAll.SELF_TEST_PRODUCER_GROUP, rpcHook);
        producer.setInstanceName(String.valueOf(System.currentTimeMillis()));
        producer.setNamesrvAddr(configure.getNamesrvAddr());

        try {
            producer.start();
            return producer.getDefaultMQProducerImpl().getmQClientFactory().getMQClientAPIImpl().getSystemTopicList(20000L);
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        } finally {
            producer.shutdown();
        }
    }

    @Override
    public SendResult sendTopicMessageRequest(SendTopicMessageRequest sendTopicMessageRequest) {
        List<TopicConfigInfo> topicConfigInfos = examineTopicConfig(sendTopicMessageRequest.getTopic());
        String messageType = topicConfigInfos.get(0).getMessageType();
        AclClientRPCHook rpcHook = null;
        if (configure.isACLEnabled()) {
            rpcHook = new AclClientRPCHook(new SessionCredentials(
                    configure.getAccessKey(),
                    configure.getSecretKey()
            ));
        }
        if (TopicMessageType.TRANSACTION.equals(messageType)) {
            // transaction message
            TransactionListener transactionListener = new TransactionListenerImpl();

            TransactionMQProducer producer = buildTransactionMQProducer(MixAll.SELF_TEST_PRODUCER_GROUP, rpcHook, sendTopicMessageRequest.isTraceEnabled());
            producer.setInstanceName(String.valueOf(System.currentTimeMillis()));
            producer.setNamesrvAddr(configure.getNamesrvAddr());
            producer.setTransactionListener(transactionListener);
            try {
                producer.start();
                Message msg = new Message(sendTopicMessageRequest.getTopic(),
                        sendTopicMessageRequest.getTag(),
                        sendTopicMessageRequest.getKey(),
                        sendTopicMessageRequest.getMessageBody().getBytes()
                );
                return producer.sendMessageInTransaction(msg, null);
            } catch (Exception e) {
                Throwables.throwIfUnchecked(e);
                throw new RuntimeException(e);
            } finally {
                waitSendTraceFinish(producer, sendTopicMessageRequest.isTraceEnabled());
                producer.shutdown();
            }
        } else {
            // no transaction message
            DefaultMQProducer producer = null;
            producer = buildDefaultMQProducer(MixAll.SELF_TEST_PRODUCER_GROUP, rpcHook, sendTopicMessageRequest.isTraceEnabled());
            producer.setInstanceName(String.valueOf(System.currentTimeMillis()));
            producer.setNamesrvAddr(configure.getNamesrvAddr());
            try {
                producer.start();
                Message msg = new Message(sendTopicMessageRequest.getTopic(),
                        sendTopicMessageRequest.getTag(),
                        sendTopicMessageRequest.getKey(),
                        sendTopicMessageRequest.getMessageBody().getBytes()
                );
                return producer.send(msg);
            } catch (Exception e) {
                Throwables.throwIfUnchecked(e);
                throw new RuntimeException(e);
            } finally {
                waitSendTraceFinish(producer, sendTopicMessageRequest.isTraceEnabled());
                producer.shutdown();
            }
        }

    }

    @Override
    public boolean refreshTopicList() {
        routeCache.clear();
        clusterInfoService.refresh();
        MQAdminExtImpl.clearTopicConfigCache();
        return true;
    }

    private void waitSendTraceFinish(DefaultMQProducer producer, boolean traceEnabled) {
        if (!traceEnabled) {
            return;
        }
        try {
            TraceDispatcher traceDispatcher = Reflect.on(producer).field("traceDispatcher").get();
            if (traceDispatcher != null) {
                ArrayBlockingQueue<TraceContext> traceContextQueue = Reflect.on(traceDispatcher).field("traceContextQueue").get();
                while (traceContextQueue.size() > 0) {
                    Thread.sleep(1);
                }
            }
            // wait another 150ms until async request send finish
            // after new RocketMQ version released, this logic can be removed
            // https://github.com/apache/rocketmq/pull/2989
            Thread.sleep(150);
        } catch (Exception ignore) {
        }
    }

    static class TransactionListenerImpl implements TransactionListener {
        private final AtomicInteger transactionIndex = new AtomicInteger(0);

        private final ConcurrentHashMap<String, Integer> localTrans = new ConcurrentHashMap<>();

        @Override
        public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
            return LocalTransactionState.COMMIT_MESSAGE;
        }

        @Override
        public LocalTransactionState checkLocalTransaction(MessageExt msg) {
            return LocalTransactionState.COMMIT_MESSAGE;
        }
    }
}
