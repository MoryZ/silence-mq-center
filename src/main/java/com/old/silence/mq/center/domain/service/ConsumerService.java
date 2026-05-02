package com.old.silence.mq.center.domain.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.utils.ThreadUtils;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.body.ConsumerConnection;
import org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.remoting.protocol.body.SubscriptionGroupWrapper;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import com.old.silence.mq.center.dto.ConsumerConfigInfo;
import com.old.silence.mq.center.dto.ConsumerGroupRollBackStat;
import com.old.silence.mq.center.dto.DeleteSubGroupRequest;
import com.old.silence.mq.center.dto.GroupConsumeInfo;
import com.old.silence.mq.center.dto.QueueStatInfo;
import com.old.silence.mq.center.dto.ResetOffsetRequest;
import com.old.silence.mq.center.dto.TopicConsumerInfo;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author moryzang
 */
@Service
public class ConsumerService implements InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerService.class);

    private final ClusterInfoService clusterInfoService;
    private final MQAdminService mqAdminService;
    private volatile boolean isCacheBeingBuilt = false;

    private static final Set<String> SYSTEM_GROUP_SET = new HashSet<>();

    private ExecutorService executorService;

    private final List<GroupConsumeInfo> cacheConsumeInfoList = Collections.synchronizedList(new ArrayList<>());

    private final HashMap<String, List<String>> consumerGroupMap = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        Runtime runtime = Runtime.getRuntime();
        int corePoolSize = Math.max(10, runtime.availableProcessors() * 2);
        int maximumPoolSize = Math.max(20, runtime.availableProcessors() * 2);
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicLong threadIndex = new AtomicLong(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "QueryGroup_" + this.threadIndex.incrementAndGet());
            }
        };
        RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardOldestPolicy();
        this.executorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5000), threadFactory, handler);
    }

    @Override
    public void destroy() {
        ThreadUtils.shutdownGracefully(executorService, 10L, TimeUnit.SECONDS);
    }

    static {
        SYSTEM_GROUP_SET.add(MixAll.TOOLS_CONSUMER_GROUP);
        SYSTEM_GROUP_SET.add(MixAll.FILTERSRV_CONSUMER_GROUP);
        SYSTEM_GROUP_SET.add(MixAll.SELF_TEST_CONSUMER_GROUP);
        SYSTEM_GROUP_SET.add(MixAll.ONS_HTTP_PROXY_GROUP);
        SYSTEM_GROUP_SET.add(MixAll.CID_ONSAPI_PULL_GROUP);
        SYSTEM_GROUP_SET.add(MixAll.CID_ONSAPI_PERMISSION_GROUP);
        SYSTEM_GROUP_SET.add(MixAll.CID_ONSAPI_OWNER_GROUP);
        SYSTEM_GROUP_SET.add(MixAll.CID_SYS_RMQ_TRANS);
        SYSTEM_GROUP_SET.add("CID_DefaultHeartBeatSyncerTopic");
    }

    public ConsumerService(ClusterInfoService clusterInfoService, MQAdminService mqAdminService) {
        this.clusterInfoService = clusterInfoService;
        this.mqAdminService = mqAdminService;
    }

    /**
     * 查询消费者组列表
     */
    public List<GroupConsumeInfo> queryGroupList(boolean skipSysGroup, String address) {
        // 优化：构建缓存时等待而不是抛异常
        synchronized (this) {
            while (isCacheBeingBuilt) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for cache build", e);
                }
            }
            if (cacheConsumeInfoList.isEmpty()) {
                isCacheBeingBuilt = true;
                try {
                    makeGroupListCache(address);
                } finally {
                    isCacheBeingBuilt = false;
                    this.notifyAll();
                }
            }
        }

        if (cacheConsumeInfoList.isEmpty()) {
            throw new RuntimeException("No consumer group information available");
        }

        // clone 返回对象，避免副作用（手动赋值，兼容无拷贝构造的 GroupConsumeInfo）
        List<GroupConsumeInfo> result = cacheConsumeInfoList.stream()
            .filter(group -> !skipSysGroup || !SYSTEM_GROUP_SET.contains(group.getGroup()))
            .map(group -> {
                GroupConsumeInfo copy = new GroupConsumeInfo();
                copy.setGroup(group.getGroup());
                copy.setConsumeType(group.getConsumeType());
                copy.setMessageModel(group.getMessageModel());
                copy.setCount(group.getCount());
                copy.setDiffTotal(group.getDiffTotal());
                copy.setSubGroupType(group.getSubGroupType());
                copy.setUpdateTime(group.getUpdateTime());
                copy.setAddress(group.getAddress());
                // 其他字段如有需要请补充
                if (!skipSysGroup && SYSTEM_GROUP_SET.contains(copy.getGroup())) {
                    copy.setGroup(String.format("%s%s", "%SYS%", copy.getGroup()));
                }
                return copy;
            })
            .collect(Collectors.toList());
        Collections.sort(result);
        return result;
    }

    public void makeGroupListCache(String address) {
        Map<String, List<String>> discoveredGroupMap = new HashMap<>();
        Map<String, SubscriptionGroupConfig> mergedSubscriptionGroupTable = new HashMap<>();
        try {
            ClusterInfo clusterInfo = clusterInfoService.get();
            for (BrokerData brokerData : clusterInfo.getBrokerAddrTable().values()) {
                if (brokerData.getBrokerAddrs() == null || brokerData.getBrokerAddrs().isEmpty()) {
                    continue;
                }

                for (String brokerAddr : brokerData.getBrokerAddrs().values()) {
                    if (StringUtils.isBlank(brokerAddr)) {
                        continue;
                    }

                    SubscriptionGroupWrapper subscriptionGroupWrapper =
                            mqAdminService.execute(admin -> admin.getAllSubscriptionGroup(brokerAddr, 30000L));
                    if (subscriptionGroupWrapper == null || subscriptionGroupWrapper.getSubscriptionGroupTable() == null) {
                        continue;
                    }

                    for (Map.Entry<String, SubscriptionGroupConfig> groupEntry : subscriptionGroupWrapper.getSubscriptionGroupTable().entrySet()) {
                        String groupName = groupEntry.getKey();
                        List<String> addresses = discoveredGroupMap.computeIfAbsent(groupName, key -> new ArrayList<>());
                        if (!addresses.contains(brokerAddr)) {
                            addresses.add(brokerAddr);
                        }
                        if (groupEntry.getValue() != null) {
                            mergedSubscriptionGroupTable.putIfAbsent(groupName, groupEntry.getValue());
                        }
                    }
                }
            }
        } catch (Exception err) {
            throw new RuntimeException(err);
        }

        if (discoveredGroupMap.isEmpty()) {
            logger.warn("No subscription group information available");
            isCacheBeingBuilt = false;
            return;
        }
        List<GroupConsumeInfo> groupConsumeInfoList = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch countDownLatch = new CountDownLatch(discoveredGroupMap.size());
        for (Map.Entry<String, List<String>> entry : discoveredGroupMap.entrySet()) {
            String consumerGroup = entry.getKey();
            executorService.submit(() -> {
                try {
                    GroupConsumeInfo consumeInfo = queryGroup(consumerGroup, address);
                    consumeInfo.setAddress(entry.getValue());
                    if (SYSTEM_GROUP_SET.contains(consumerGroup)) {
                        consumeInfo.setSubGroupType("SYSTEM");
                    } else {
                        try {
                            SubscriptionGroupConfig config = mergedSubscriptionGroupTable.get(consumerGroup);
                            consumeInfo.setSubGroupType(config != null && config.isConsumeMessageOrderly() ? "FIFO" : "NORMAL");
                        } catch (NullPointerException e) {
                            logger.warn("SubscriptionGroupConfig not found for consumer group: {}", consumerGroup);
                            boolean isFifoType = examineSubscriptionGroupConfig(consumerGroup)
                                    .stream().map(ConsumerConfigInfo::getSubscriptionGroupConfig)
                                    .allMatch(SubscriptionGroupConfig::isConsumeMessageOrderly);
                            consumeInfo.setSubGroupType(isFifoType ? "FIFO" : "NORMAL");
                        }
                    }
                    consumeInfo.setUpdateTime(Instant.now());
                    groupConsumeInfoList.add(consumeInfo);
                } catch (Exception e) {
                    logger.error("queryGroup exception, consumerGroup: {}", consumerGroup, e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interruption occurred while waiting for task completion", e);
        }
        logger.info("All consumer group query tasks have been completed");
        isCacheBeingBuilt = false;
        Collections.sort(groupConsumeInfoList);

        synchronized (consumerGroupMap) {
            consumerGroupMap.clear();
            consumerGroupMap.putAll(discoveredGroupMap);
        }

        cacheConsumeInfoList.clear();
        cacheConsumeInfoList.addAll(groupConsumeInfoList);
    }

    /**
     * 刷新单个消费者组
     */
    public GroupConsumeInfo refreshGroup(String address, String consumerGroup) throws Exception {
            if (isCacheBeingBuilt || cacheConsumeInfoList.isEmpty()) {
                throw new RuntimeException("Cache is being built or empty, please try again later");
            }
            synchronized (cacheConsumeInfoList) {
                for (int i = 0; i < cacheConsumeInfoList.size(); i++) {
                    GroupConsumeInfo groupConsumeInfo = cacheConsumeInfoList.get(i);
                    if (groupConsumeInfo.getGroup().equals(consumerGroup)) {
                        GroupConsumeInfo updatedInfo = queryGroup(consumerGroup, address);
                        updatedInfo.setUpdateTime(Instant.now());
                        updatedInfo.setGroup(consumerGroup);
                        updatedInfo.setAddress(consumerGroupMap.get(consumerGroup));
                        cacheConsumeInfoList.set(i, updatedInfo);
                        return updatedInfo;
                    }
                }
            }
            throw new RuntimeException("No consumer group information available");
    }

    /**
     * 刷新所有消费者组
     */
    public List<GroupConsumeInfo> refreshAllGroup(String address) throws Exception {

        cacheConsumeInfoList.clear();
        consumerGroupMap.clear();
        return queryGroupList(false, address);
    }

    /**
     * 查询单个消费者组信息
     */
    public GroupConsumeInfo queryGroup(String consumerGroup, String address) throws Exception {
        return mqAdminService.execute(admin -> {
            GroupConsumeInfo groupInfo = new GroupConsumeInfo();
            groupInfo.setGroup(consumerGroup);

            try {
                // address 为空时，走默认查询，避免因 null 地址导致全量告警
                ConsumerConnection consumerConnection;
                if (StringUtils.isBlank(address)) {
                    consumerConnection = admin.examineConsumerConnectionInfo(consumerGroup);
                } else {
                    try {
                        consumerConnection = admin.examineConsumerConnectionInfo(consumerGroup, address);
                    } catch (Exception ignored) {
                        // 指定地址查询失败时，回退到默认查询，提升兼容性
                        consumerConnection = admin.examineConsumerConnectionInfo(consumerGroup);
                    }
                }
                if (consumerConnection != null) {
                    groupInfo.setConsumeType(consumerConnection.getConsumeType());
                    groupInfo.setMessageModel(consumerConnection.getMessageModel());
                    groupInfo.setCount(consumerConnection.getConnectionSet() != null ? consumerConnection.getConnectionSet().size() : 0);
                }

                groupInfo.setDiffTotal(-1);
                groupInfo.setUpdateTime(Instant.now());
            } catch (Exception e) {
                // 异常处理：查询消费者连接信息失败
                logger.debug("Failed to query consumer connection for group: {} at address: {}", consumerGroup, address, e);
                groupInfo.setDiffTotal(-1);
                groupInfo.setUpdateTime(Instant.now());
            }

            return groupInfo;
        });
    }

    /**
     * 重置消费者偏移量
     */
    public Map<String, ConsumerGroupRollBackStat> resetOffset(ResetOffsetRequest request) throws Exception {
        Map<String, ConsumerGroupRollBackStat> resultMap = new HashMap<>();

        if (request == null || request.getConsumerGroupList() == null || request.getConsumerGroupList().isEmpty()) {
            return resultMap;
        }

        return mqAdminService.execute(admin -> {
            Map<String, ConsumerGroupRollBackStat> result = new HashMap<>();

            for (String consumerGroup : request.getConsumerGroupList()) {
                try {
                    // 重置偏移量
                    admin.resetOffsetByTimestamp(request.getTopic(), consumerGroup,
                            request.getResetTime(), request.isForce());

                    ConsumerGroupRollBackStat stat = new ConsumerGroupRollBackStat(true);
                    result.put(consumerGroup, stat);
                } catch (Exception e) {
                    ConsumerGroupRollBackStat stat = new ConsumerGroupRollBackStat(false, e.getMessage());
                    result.put(consumerGroup, stat);
                }
            }

            return result;
        });
    }

    /**
     * 查询订阅组配置
     */
    public List<ConsumerConfigInfo> examineSubscriptionGroupConfig(String consumerGroup) throws Exception {
        return mqAdminService.execute(admin -> {
            List<ConsumerConfigInfo> configList = new ArrayList<>();

            try {
                // 获取集群信息以获取所有broker地址
                var clusterInfo = admin.examineBrokerClusterInfo();
                if (clusterInfo == null || clusterInfo.getBrokerAddrTable() == null) {
                    logger.warn("Failed to get cluster info or broker address table is empty");
                    return configList;
                }

                // 遍历所有broker，查询订阅组配置
                for (var brokerEntry : clusterInfo.getBrokerAddrTable().entrySet()) {
                    try {
                        var brokerAddrs = brokerEntry.getValue();
                        if (brokerAddrs != null && brokerAddrs.getBrokerAddrs() != null) {
                            // 取master节点地址
                            String masterAddr = brokerAddrs.getBrokerAddrs().get(0L);
                            if (StringUtils.isNotBlank(masterAddr)) {
                                try {
                                    SubscriptionGroupConfig config = admin.examineSubscriptionGroupConfig(masterAddr, consumerGroup);
                                    if (config != null) {
                                        ConsumerConfigInfo configInfo = new ConsumerConfigInfo();
                                        configInfo.setSubscriptionGroupConfig(config);
                                        configList.add(configInfo);
                                        break; // 通常只需获取一个有效的配置
                                    }
                                } catch (Exception e) {
                                    // 继续查询其他broker
                                    logger.debug("Failed to query subscription group config from broker: {}, error: {}", masterAddr, e.getMessage());
                                }
                            }
                        }
                    } catch (Exception e) {
                        // 继续查询其他broker
                        logger.debug("Failed to process broker: {}, error: {}", brokerEntry.getKey(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                // 异常处理：获取集群信息失败
                logger.error("Failed to examine subscription group config for group: {}", consumerGroup, e);
            }

            return configList;
        });
    }

    /**
     * 删除订阅组
     */
    public void deleteSubGroup(DeleteSubGroupRequest request) throws Exception {
        if (request == null || StringUtils.isBlank(request.getGroupName())) {
            return;
        }

        mqAdminService.execute(admin -> {
            try {
                // 如果指定了broker列表，从这些broker删除
                if (request.getBrokerNameList() != null && !request.getBrokerNameList().isEmpty()) {
                    for (String brokerAddr : request.getBrokerNameList()) {
                        try {
                            admin.deleteSubscriptionGroup(brokerAddr, request.getGroupName());
                        } catch (Exception e) {
                            // 继续删除其他broker上的配置
                            logger.warn("Failed to delete subscription group: {} from broker: {}, error: {}",
                                    request.getGroupName(), brokerAddr, e.getMessage());
                        }
                    }
                } else {
                    // 如果未指定broker，则从所有broker删除该订阅组
                    try {
                        var clusterInfo = admin.examineBrokerClusterInfo();
                        if (clusterInfo != null && clusterInfo.getBrokerAddrTable() != null) {
                            for (var brokerEntry : clusterInfo.getBrokerAddrTable().entrySet()) {
                                try {
                                    var brokerAddrs = brokerEntry.getValue();
                                    if (brokerAddrs != null && brokerAddrs.getBrokerAddrs() != null) {
                                        // 取master节点地址
                                        String masterAddr = brokerAddrs.getBrokerAddrs().get(0L);
                                        if (StringUtils.isNotBlank(masterAddr)) {
                                            try {
                                                admin.deleteSubscriptionGroup(masterAddr, request.getGroupName());
                                            } catch (Exception e) {
                                                // 继续删除其他broker
                                                logger.warn("Failed to delete subscription group: {} from broker: {}, error: {}",
                                                        request.getGroupName(), masterAddr, e.getMessage());
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    // 继续处理其他broker
                                    logger.debug("Failed to process broker: {}, error: {}", brokerEntry.getKey(), e.getMessage());
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Failed to get cluster info for deleting subscription group: {}", request.getGroupName(), e);
                    }
                }
            } catch (Exception e) {
                // 异常处理：删除订阅组失败
                logger.error("Failed to delete subscription group: {}", request.getGroupName(), e);
            }
            return null;
        });
    }

    /**
     * 创建或更新订阅组配置
     */
    public Boolean createAndUpdateSubscriptionGroupConfig(ConsumerConfigInfo consumerConfigInfo) throws Exception {
        if (consumerConfigInfo == null || consumerConfigInfo.getSubscriptionGroupConfig() == null) {
            return false;
        }

        return mqAdminService.execute(admin -> {
            try {
                SubscriptionGroupConfig config = consumerConfigInfo.getSubscriptionGroupConfig();

                List<String> brokerNameList = consumerConfigInfo.getBrokerNameList();
                List<String> clusterNameList = consumerConfigInfo.getClusterNameList();

                // 如果指定了 broker，则在这些 broker 上创建配置
                if (brokerNameList != null && !brokerNameList.isEmpty()) {
                    for (String brokerAddr : brokerNameList) {
                        try {
                            admin.createAndUpdateSubscriptionGroupConfig(brokerAddr, config);
                        } catch (Exception e) {
                            logger.warn("Failed to create or update subscription group: {} on broker: {}, error: {}",
                                    config.getGroupName(), brokerAddr, e.getMessage());
                        }
                    }
                    return true;
                }

                // 否则在集群的所有 broker 上创建配置
                if (clusterNameList != null && !clusterNameList.isEmpty()) {
                    try {
                        // 获取集群信息
                        var clusterInfo = admin.examineBrokerClusterInfo();
                        if (clusterInfo != null && clusterInfo.getClusterAddrTable() != null) {
                            for (String clusterName : clusterNameList) {
                                // 找到该集群的broker列表
                                var brokerNameSet = clusterInfo.getClusterAddrTable().get(clusterName);
                                if (brokerNameSet != null && !brokerNameSet.isEmpty()) {
                                    // 在集群的所有broker上创建配置
                                    for (String brokerName : brokerNameSet) {
                                        try {
                                            var brokerAddrs = clusterInfo.getBrokerAddrTable().get(brokerName);
                                            if (brokerAddrs != null && brokerAddrs.getBrokerAddrs() != null) {
                                                // 取master节点地址
                                                String masterAddr = brokerAddrs.getBrokerAddrs().get(0L);
                                                if (StringUtils.isNotBlank(masterAddr)) {
                                                    admin.createAndUpdateSubscriptionGroupConfig(masterAddr, config);
                                                }
                                            }
                                        } catch (Exception e) {
                                            // 继续处理该集群的其他broker
                                            logger.warn("Failed to create or update subscription group: {} on broker: {} in cluster: {}, error: {}",
                                                    config.getGroupName(), brokerName, clusterName, e.getMessage());
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Failed to get cluster info for creating subscription group: {}", config.getGroupName(), e);
                    }
                    return true;
                }

                return false;
            } catch (Exception e) {
                // 异常处理：创建订阅组配置失败
                logger.error("Failed to create or update subscription group config", e);
                return false;
            }
        });
    }

    /**
     * 获取订阅组所在的 broker 名称集合
     */
    public Set<String> fetchBrokerNameSetBySubscriptionGroup(String consumerGroup) throws Exception {
        return mqAdminService.execute(admin -> {
            Set<String> brokerNameSet = new HashSet<>();

            try {
                // 获取消费者连接信息，从中提取 broker 信息
                ConsumerConnection consumerConnection = admin.examineConsumerConnectionInfo(consumerGroup);
                if (consumerConnection != null && consumerConnection.getConnectionSet() != null) {
                    // 从连接集合中提取 broker 信息
                    consumerConnection.getConnectionSet().forEach(conn -> {
                        if (conn != null && StringUtils.isNotBlank(conn.getClientAddr())) {
                            brokerNameSet.add(conn.getClientAddr().split(":")[0]);
                        }
                    });
                }
            } catch (Exception e) {
                // 异常处理：获取消费者连接失败
                logger.error("Failed to fetch broker name set for consumer group: {}", consumerGroup, e);
            }

            return brokerNameSet;
        });
    }

    public List<TopicConsumerInfo> queryTopicConsumerInfo(String topic, String consumerGroup) throws Exception {
        return mqAdminService.execute(admin -> toTopicConsumerInfoList(topic, admin.examineConsumeStats(consumerGroup, topic), consumerGroup));
    }

    private List<TopicConsumerInfo> toTopicConsumerInfoList(String topic, ConsumeStats consumeStats, String consumerGroup) throws Exception {
        if (consumeStats == null || consumeStats.getOffsetTable() == null || consumeStats.getOffsetTable().isEmpty()) {
            return Collections.emptyList();
        }

        var messageQueues = consumeStats.getOffsetTable().keySet()
                .stream()
                .filter(messageQueue -> StringUtils.isBlank(topic) || StringUtils.equals(messageQueue.getTopic(), topic))
                .sorted(Comparator
                        .comparing(MessageQueue::getTopic)
                        .thenComparing(MessageQueue::getBrokerName)
                        .thenComparingInt(MessageQueue::getQueueId))
                .toList();

        if (messageQueues.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, TopicConsumerInfo> topicConsumerInfoMap = new LinkedHashMap<>();
        var messageQueueClientMap = getClientConnection(consumerGroup);

        for (var messageQueue : messageQueues) {
            TopicConsumerInfo topicConsumerInfo = topicConsumerInfoMap.computeIfAbsent(
                    messageQueue.getTopic(), TopicConsumerInfo::new);

            QueueStatInfo queueStatInfo = QueueStatInfo.fromOffsetTableEntry(messageQueue, consumeStats.getOffsetTable().get(messageQueue));
            queueStatInfo.setClientInfo(messageQueueClientMap.get(messageQueue));
            topicConsumerInfo.appendQueueStatInfo(queueStatInfo);
        }

        return new ArrayList<>(topicConsumerInfoMap.values());
    }

    private Map<MessageQueue, String> getClientConnection(String consumerGroup) throws Exception {
        Map<MessageQueue, String> results = new HashMap<>();
        mqAdminService.executeVoid(admin -> {
            var consumerConnection = admin.examineConsumerConnectionInfo(consumerGroup);
            if (consumerConnection == null || consumerConnection.getConnectionSet() == null) {
                return;
            }

            consumerConnection.getConnectionSet().forEach(conn -> {
                if (conn == null || StringUtils.isBlank(conn.getClientId())) {
                    return;
                }

                try {
                    var clientId = conn.getClientId();
                    var consumerRunningInfo = admin.getConsumerRunningInfo(consumerGroup, clientId, false);
                    if (consumerRunningInfo != null && consumerRunningInfo.getMqTable() != null) {
                        consumerRunningInfo.getMqTable().keySet().forEach(messageQueue -> {
                            results.put(messageQueue, clientId);
                        });
                    }
                } catch (Exception e) {
                    logger.debug("Failed to get running info for consumerGroup: {}, clientId: {}, error: {}",
                            consumerGroup, conn.getClientId(), e.getMessage());
                }
            });
        });
        return results;
    }

    /**
     * 按消费者组查询消费统计信息
     */
    public List<TopicConsumerInfo> queryConsumeStatsListByGroupName(String consumerGroup, String address) throws Exception {
        return mqAdminService.execute(admin -> {
            List<TopicConsumerInfo> topicConsumerList = new ArrayList<>();
            try {
                ConsumerConnection consumerConnection = admin.examineConsumerConnectionInfo(consumerGroup, address);
                if (consumerConnection == null || consumerConnection.getConnectionSet() == null || consumerConnection.getConnectionSet().isEmpty()) {
                    return topicConsumerList;
                }
                Set<String> allTopics = admin.fetchAllTopicList().getTopicList();
                if (allTopics != null && !allTopics.isEmpty()) {
                    for (String topic : allTopics) {
                        try {
                            TopicConsumerInfo topicInfo = new TopicConsumerInfo(topic);
                            long totalDiffTotal = 0;
                            try {
                                var topicRoute = admin.examineTopicRouteInfo(topic);
                                if (topicRoute != null && topicRoute.getQueueDatas() != null && !topicRoute.getQueueDatas().isEmpty()) {
                                    int queueId = 0;
                                    for (var queueData : topicRoute.getQueueDatas()) {
                                        try {
                                            if (queueData != null && queueData.getBrokerName() != null) {
                                                MessageQueue mq = new MessageQueue(topic, queueData.getBrokerName(), queueId);
                                                long brokerOffset = admin.maxOffset(mq);
                                                totalDiffTotal += brokerOffset;
                                            }
                                            queueId++;
                                        } catch (Exception e) {
                                            logger.debug("Failed to get max offset for topic: {}, queue: {}, error: {}", topic, queueId, e.getMessage());
                                            queueId++;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // 跳过计算
                                logger.warn("Failed to examine topic route info for topic: {}, error: {}", topic, e.getMessage());
                            }
                            topicInfo.setDiffTotal(totalDiffTotal);
                            topicConsumerList.add(topicInfo);
                        } catch (Exception e) {
                            // 跳过该topic
                            logger.warn("Failed to process topic: {} for consumer group: {}, error: {}", topic, consumerGroup, e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                // 异常处理：查询消费统计失败
                logger.error("Failed to query consume stats for consumer group: {} at address: {}", consumerGroup, address, e);
            }
            return topicConsumerList;
        });
    }

    /**
     * 获取消费者连接信息
     */
    public ConsumerConnection getConsumerConnection(String consumerGroup, String address) throws Exception {
        return mqAdminService.execute(admin -> {
            try {
                return admin.examineConsumerConnectionInfo(consumerGroup, address);
            } catch (Exception e) {
                // 返回空的消费者连接对象
                logger.warn("Failed to get consumer connection for group: {} at address: {}", consumerGroup, address, e);
                return new ConsumerConnection();
            }
        });
    }

    /**
     * 获取消费者运行信息
     */
    public ConsumerRunningInfo getConsumerRunningInfo(String consumerGroup, String clientId, boolean jstack) throws Exception {
        return mqAdminService.execute(admin -> {
            try {
                return admin.getConsumerRunningInfo(consumerGroup, clientId, jstack);
            } catch (Exception e) {
                // 返回空的运行信息
                logger.warn("Failed to get consumer running info for group: {}, clientId: {}", consumerGroup, clientId, e);
                return new ConsumerRunningInfo();
            }
        });
    }


    public String getConsumerGroup(String consumerGroup) {
        if (consumerGroup != null && consumerGroup.startsWith("%SYS%")) {
            return consumerGroup.substring(5); // Remove "%SYS%" prefix
        }
        return consumerGroup;
    }
}
