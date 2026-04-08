package com.old.silence.mq.center.domain.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.body.ConsumerConnection;
import org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.old.silence.mq.center.dto.ConsumerGroupRollBackStat;
import com.old.silence.mq.center.dto.GroupConsumeInfo;
import com.old.silence.mq.center.dto.TopicConsumerInfo;
import com.old.silence.mq.center.dto.ConsumerConfigInfo;
import com.old.silence.mq.center.dto.DeleteSubGroupRequest;
import com.old.silence.mq.center.dto.ResetOffsetRequest;

/**
 * @author moryzang
 */
@Service
public class ConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerService.class);

    private final MQAdminService mqAdminService;

    public ConsumerService(MQAdminService mqAdminService) {
        this.mqAdminService = mqAdminService;
    }

    /**
     * 查询消费者组列表
     */
    public List<GroupConsumeInfo> queryGroupList(boolean skipSysGroup, String address) throws Exception {
        return mqAdminService.execute(admin -> {
            List<GroupConsumeInfo> groupList = new ArrayList<>();
            
            try {
                // 获取集群信息，从中提取所有broker地址
                var clusterInfo = admin.examineBrokerClusterInfo();
                if (clusterInfo == null || clusterInfo.getBrokerAddrTable() == null) {
                    logger.warn("Failed to get cluster info or broker address table is empty");
                    return groupList;
                }

                // 从所有broker的配置中获取消费者组列表
                // 注意：RocketMQ 5.3.1 无法直接获取所有消费者组列表，因为没有相关的API
                // 实际应用中应该通过以下方案获取：
                // 1. 配置中心（如 Apollo、Nacos）维护消费者组列表
                // 2. 监控系统（如 Prometheus）收集的消费者组数据
                // 3. 消费者端主动上报自身信息
                // 4. 外部数据库表维护消费者组映射关系
                Set<String> consumerGroupSet = new HashSet<>();
                
                // 如果需要从 RocketMQ 获取消费者组，只能通过以下替代方案：
                // 扫描每个消费者的主题订阅情况，反向推导消费者组
                try {
                    Set<String> allTopics = admin.fetchAllTopicList().getTopicList();
                    if (allTopics != null && !allTopics.isEmpty()) {
                        for (String topic : allTopics) {
                            try {
                                // 获取主题的路由信息，从中检查是否有消费者
                                var topicRoute = admin.examineTopicRouteInfo(topic);
                                if (topicRoute != null && !topicRoute.getQueueDatas().isEmpty()) {
                                    // 这里只能知道topic存在，无法直接获取消费者组
                                    // 因此此方案也不可行
                                }
                            } catch (Exception e) {
                                logger.debug("Failed to examine topic: {}, error: {}", topic, e.getMessage());
                            }
                        }
                    }
                    logger.warn("Cannot fetch consumer groups from RocketMQ 5.3.1 - no direct API available. " +
                        "Recommend using external configuration system, monitoring data, or consumer self-registration.");
                } catch (Exception e) {
                    logger.error("Failed to fetch all topics for consumer group discovery: {}", e.getMessage(), e);
                }

                // 构建消费者组信息列表
                for (String groupName : consumerGroupSet) {
                    // 过滤系统消费者组
                    if (skipSysGroup && groupName.startsWith("__")) {
                        continue;
                    }
                    
                    try {
                        GroupConsumeInfo info = queryGroup(groupName, address);
                        if (info != null) {
                            groupList.add(info);
                        }
                    } catch (Exception e) {
                        // 跳过查询失败的组
                        logger.warn("Failed to query consumer group: {} at address: {}, error: {}", groupName, address, e.getMessage());
                    }
                }
            } catch (Exception e) {
                // 日志或异常处理
                logger.error("Failed to query consumer group list at address: {}", address, e);
            }
            
            return groupList;
        });
    }

    /**
     * 刷新单个消费者组
     */
    public GroupConsumeInfo refreshGroup(String address, String consumerGroup) throws Exception {
        return queryGroup(consumerGroup, address);
    }

    /**
     * 刷新所有消费者组
     */
    public List<GroupConsumeInfo> refreshAllGroup(String address) throws Exception {
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
                // 获取消费者连接信息
                ConsumerConnection consumerConnection = admin.examineConsumerConnectionInfo(consumerGroup, address);
                if (consumerConnection != null) {
                    groupInfo.setConsumeType(consumerConnection.getConsumeType());
                    groupInfo.setMessageModel(consumerConnection.getMessageModel());
                    groupInfo.setCount(consumerConnection.getConnectionSet() != null ? consumerConnection.getConnectionSet().size() : 0);
                }
                
                groupInfo.setDiffTotal(-1);
                groupInfo.setUpdateTime(new java.util.Date());
            } catch (Exception e) {
                // 异常处理：查询消费者连接信息失败
                logger.warn("Failed to query consumer connection for group: {} at address: {}", consumerGroup, address, e);
                groupInfo.setDiffTotal(-1);
                groupInfo.setUpdateTime(new java.util.Date());
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

    /**
     * 按消费者组查询消费统计信息
     */
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
}
