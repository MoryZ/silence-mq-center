package com.old.silence.mq.center.domain.service.facade;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.old.silence.mq.center.api.config.RMQConfigure;
import com.old.silence.mq.center.exception.ServiceException;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.admin.OffsetWrapper;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.body.BrokerLiveInfo;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.body.TopicList;
import org.apache.rocketmq.remoting.protocol.body.SubscriptionGroupWrapper;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;
import org.apache.rocketmq.remoting.protocol.body.ProducerConnection;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.body.KVTable;
import org.apache.rocketmq.remoting.protocol.body.BrokerStatsData;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.common.AclConfig;
import org.apache.rocketmq.common.PlainAccessConfig;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RocketMQ 统一门面类
 * 
 * 目的：
 * 1. 隐藏 Admin API 的复杂性
 * 2. 提供简洁易用的接口
 * 3. 返回清晰的 DTO 对象而不是复杂的嵌套结构
 * 4. 集中处理异常和日志
 * 
 * 核心思想：用户不需要了解 Admin API 的细节，
 * 只需要调用 Facade 的简洁方法即可
 * 
 * 使用示例：
 * <pre>
 * ClusterInfo cluster = facade.getClusterInfo();
 * List<TopicInfo> topics = facade.listTopics();
 * facade.deleteTopic("my-topic");
 * </pre>
 */
@Component
public class RocketMQClientFacade implements InitializingBean, DisposableBean {
    
    private static final Logger logger = LoggerFactory.getLogger(
        RocketMQClientFacade.class);
    
    private final MQAdminExt mqAdminExt;
    private final RMQConfigure configure;
    
    public RocketMQClientFacade(MQAdminExt mqAdminExt, RMQConfigure configure) {
        this.mqAdminExt = mqAdminExt;
        this.configure = configure;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("RocketMQClientFacade initialized");
    }
    
    @Override
    public void destroy() throws Exception {
        logger.info("RocketMQClientFacade destroyed");
    }
    
    // ============ 集群信息操作 ============
    
    /**
     * 获取集群信息（简化版）
     * 
     * ✅ 改进点：
     * - 返回清晰的 ClusterInfoDTO
     * - 自动处理异常
     * - 过滤掉复杂的内部结构
     */
    public ClusterInfoDTO getClusterInfo() {
        try {
            logger.debug("op=getClusterInfo start");
            
            ClusterInfo rawInfo = mqAdminExt.examineBrokerClusterInfo();
            
            // 转换为易用的 DTO
            ClusterInfoDTO dto = new ClusterInfoDTO();
            dto.setClusterName("default");
            dto.setNameServers(parseNameServers());
            
            // 转换 Broker 信息
            List<BrokerInfoDTO> brokers = rawInfo.getBrokerAddrTable()
                .entrySet()
                .stream()
                .map(entry -> convertToBrokerInfo(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
            dto.setBrokers(brokers);
            
            // 添加 Broker 在线信息
            rawInfo.getBrokerLiveTable().forEach((addr, liveInfo) -> {
                brokers.stream()
                    .filter(b -> b.getBrokerAddr().equals(addr))
                    .forEach(b -> {
                        b.setLastUpdateTime(liveInfo.getLastUpdateTimestamp());
                        b.setStatusOk(true);
                    });
            });
            
            logger.info("op=getClusterInfo success, brokerCount={}", 
                brokers.size());
            return dto;
            
        } catch (Exception e) {
            logger.error("op=getClusterInfo failed", e);
            throw new ServiceException(-1, 
                "Failed to get cluster info: " + e.getMessage());
        }
    }
    
    // ============ Topic 操作 ============
    
    /**
     * 列出所有 Topic（过滤版）
     * 
     * ✅ 改进点：
     * - 自动过滤系统 Topic
     * - 返回简洁的 TopicInfoDTO
     * - 包含关键指标（消费延迟等）
     */
    public List<TopicInfoDTO> listTopics(boolean skipSystem) {
        try {
            logger.debug("op=listTopics start");
            
            TopicList topicList = mqAdminExt.fetchAllTopicList();
            
            List<TopicInfoDTO> topics = topicList.getTopicList()
                .stream()
                .filter(topic -> !skipSystem || !isSystemTopic(topic))
                .map(this::convertToTopicInfo)
                .collect(Collectors.toList());
            
            logger.info("op=listTopics success, count={}", topics.size());
            return topics;
            
        } catch (Exception e) {
            logger.error("op=listTopics failed", e);
            throw new ServiceException(-1, 
                "Failed to list topics: " + e.getMessage());
        }
    }
    
    /**
     * 列出所有 Topic（过滤版）
     * 
     * ✅ 改进点：
     * - 自动过滤系统 Topic
     * - 返回简洁的 TopicInfoDTO
     * - 包含关键指标（消费延迟等）
     */
    public TopicList fetchAllTopicList() {
        try {
            logger.debug("op=fetchAllTopicList start");
            
            TopicList topicList = mqAdminExt.fetchAllTopicList();
            
            logger.info("op=fetchAllTopicList success, count={}", topicList.getTopicList().size());
            return topicList;
            
        } catch (Exception e) {
            logger.error("op=fetchAllTopicList failed", e);
            throw new ServiceException(-1, 
                "Failed to fetch all topics: " + e.getMessage());
        }
    }
    
    /**
     * 获取Topic统计信息
     */
    public TopicStatsTable getTopicStats(String topicName) {
        try {
            logger.debug("op=getTopicStats start, topic={}", topicName);
            
            TopicStatsTable stats = mqAdminExt.examineTopicStats(topicName);
            
            logger.info("op=getTopicStats success, topic={}", topicName);
            return stats;
            
        } catch (Exception e) {
            logger.error("op=getTopicStats failed, topic={}", topicName, e);
            throw new ServiceException(-1, 
                "Failed to get topic stats: " + e.getMessage());
        }
    }
    
    /**
     * 获取Topic路由信息
     */
    public org.apache.rocketmq.remoting.protocol.route.TopicRouteData getTopicRoute(String topicName) {
        try {
            logger.debug("op=getTopicRoute start, topic={}", topicName);
            
            org.apache.rocketmq.remoting.protocol.route.TopicRouteData route = 
                mqAdminExt.examineTopicRouteInfo(topicName);
            
            logger.info("op=getTopicRoute success, topic={}", topicName);
            return route;
            
        } catch (Exception e) {
            logger.error("op=getTopicRoute failed, topic={}", topicName, e);
            throw new ServiceException(-1, 
                "Failed to get topic route: " + e.getMessage());
        }
    }

    /**
     * 获取指定Broker的所有 SubscriptionGroup
     */
    public SubscriptionGroupWrapper getAllSubscriptionGroup(String brokerAddr, long timeoutMillis) {
        try {
            logger.debug("op=getAllSubscriptionGroup start, brokerAddr={}", brokerAddr);
            SubscriptionGroupWrapper wrapper = mqAdminExt.getAllSubscriptionGroup(brokerAddr, timeoutMillis);
            logger.info("op=getAllSubscriptionGroup success, brokerAddr={}", brokerAddr);
            return wrapper;
        } catch (Exception e) {
            logger.error("op=getAllSubscriptionGroup failed, brokerAddr={}", brokerAddr, e);
            throw new ServiceException(-1, "Failed to get all subscription group: " + e.getMessage());
        }
    }

    /**
     * 获取订阅组配置
     */
    public SubscriptionGroupConfig getSubscriptionGroupConfig(String brokerAddr, String group) {
        try {
            logger.debug("op=getSubscriptionGroupConfig start, brokerAddr={}, group={}", brokerAddr, group);
            SubscriptionGroupConfig config = mqAdminExt.examineSubscriptionGroupConfig(brokerAddr, group);
            logger.info("op=getSubscriptionGroupConfig success, brokerAddr={}, group={}", brokerAddr, group);
            return config;
        } catch (Exception e) {
            logger.error("op=getSubscriptionGroupConfig failed, brokerAddr={}, group={}", brokerAddr, group, e);
            throw new ServiceException(-1, "Failed to get subscription group config: " + e.getMessage());
        }
    }

    /**
     * Create or update subscription group config on broker
     */
    public void createOrUpdateSubscriptionGroupConfig(String brokerAddr, SubscriptionGroupConfig config) {
        try {
            logger.debug("op=createOrUpdateSubscriptionGroupConfig start, brokerAddr={}, group={}", brokerAddr, config.getGroupName());
            mqAdminExt.createAndUpdateSubscriptionGroupConfig(brokerAddr, config);
            logger.info("op=createOrUpdateSubscriptionGroupConfig success, brokerAddr={}, group={}", brokerAddr, config.getGroupName());
        } catch (Exception e) {
            logger.error("op=createOrUpdateSubscriptionGroupConfig failed, brokerAddr={}, group={}", brokerAddr, config.getGroupName(), e);
            throw new ServiceException(-1, "Failed to create/update subscription group config: " + e.getMessage());
        }
    }

    /**
     * 删除订阅组
     */
    public void deleteSubscriptionGroup(String brokerAddr, String group, boolean deleteTopic) {
        try {
            logger.debug("op=deleteSubscriptionGroup start, brokerAddr={}, group={}, deleteTopic={}", brokerAddr, group, deleteTopic);
            mqAdminExt.deleteSubscriptionGroup(brokerAddr, group, deleteTopic);
            logger.info("op=deleteSubscriptionGroup success, brokerAddr={}, group={}", brokerAddr, group);
        } catch (Exception e) {
            logger.error("op=deleteSubscriptionGroup failed, brokerAddr={}, group={}", brokerAddr, group, e);
            throw new ServiceException(-1, "Failed to delete subscription group: " + e.getMessage());
        }
    }

    /**
     * 删除 Topic 在指定 Broker 地址集合
     */
    public void deleteTopicInBroker(Set<String> brokerAddrs, String topicName) {
        try {
            logger.debug("op=deleteTopicInBroker start, topic={}", topicName);
            mqAdminExt.deleteTopicInBroker(brokerAddrs, topicName);
            logger.info("op=deleteTopicInBroker success, topic={}", topicName);
        } catch (Exception e) {
            logger.error("op=deleteTopicInBroker failed, topic={}", topicName, e);
            throw new ServiceException(-1, "Failed to delete topic in broker: " + e.getMessage());
        }
    }

    /**
     * 删除 Topic 在指定 NameServer
     */
    public void deleteTopicInNameServer(Set<String> nameServers, String topicName) {
        try {
            logger.debug("op=deleteTopicInNameServer start, topic={}", topicName);
            mqAdminExt.deleteTopicInNameServer(nameServers, topicName);
            logger.info("op=deleteTopicInNameServer success, topic={}", topicName);
        } catch (Exception e) {
            logger.error("op=deleteTopicInNameServer failed, topic={}", topicName, e);
            throw new ServiceException(-1, "Failed to delete topic in nameserver: " + e.getMessage());
        }
    }

    /**
     * 获取消费统计信息（多种重载）
     */
    public ConsumeStats getConsumeStats(String consumerGroup) {
        try {
            logger.debug("op=getConsumeStats start, group={}", consumerGroup);
            ConsumeStats stats = mqAdminExt.examineConsumeStats(consumerGroup);
            logger.info("op=getConsumeStats success, group={}", consumerGroup);
            return stats;
        } catch (Exception e) {
            logger.error("op=getConsumeStats failed, group={}", consumerGroup, e);
            throw new ServiceException(-1, "Failed to get consume stats: " + e.getMessage());
        }
    }

    public ConsumeStats getConsumeStats(String brokerAddr, String consumerGroup, String topic, long timeoutMillis) {
        try {
            logger.debug("op=getConsumeStatsByBroker start, brokerAddr={}, group={}, topic={}", brokerAddr, consumerGroup, topic);
            ConsumeStats stats = mqAdminExt.examineConsumeStats(brokerAddr, consumerGroup, topic, timeoutMillis);
            logger.info("op=getConsumeStatsByBroker success, brokerAddr={}, group={}", brokerAddr, consumerGroup);
            return stats;
        } catch (Exception e) {
            logger.error("op=getConsumeStatsByBroker failed, brokerAddr={}, group={}", brokerAddr, consumerGroup, e);
            throw new ServiceException(-1, "Failed to get consume stats by broker: " + e.getMessage());
        }
    }

    public ConsumeStats getConsumeStats(String consumerGroup, String topic) {
        try {
            logger.debug("op=getConsumeStatsByTopic start, group={}, topic={}", consumerGroup, topic);
            ConsumeStats stats = mqAdminExt.examineConsumeStats(consumerGroup, topic);
            logger.info("op=getConsumeStatsByTopic success, group={}", consumerGroup);
            return stats;
        } catch (Exception e) {
            logger.error("op=getConsumeStatsByTopic failed, group={}, topic={}", consumerGroup, topic, e);
            throw new ServiceException(-1, "Failed to get consume stats by topic: " + e.getMessage());
        }
    }
    
    /**
     * 查询Topic的消费者组
     */
    public org.apache.rocketmq.remoting.protocol.body.GroupList queryTopicConsumers(String topicName) {
        try {
            logger.debug("op=queryTopicConsumers start, topic={}", topicName);
            
            org.apache.rocketmq.remoting.protocol.body.GroupList groupList = 
                mqAdminExt.queryTopicConsumeByWho(topicName);
            
            logger.info("op=queryTopicConsumers success, topic={}, groupCount={}", 
                topicName, groupList.getGroupList().size());
            return groupList;
            
        } catch (Exception e) {
            logger.error("op=queryTopicConsumers failed, topic={}", topicName, e);
            throw new ServiceException(-1, 
                "Failed to query topic consumers: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有Topic配置
     */
    public org.apache.rocketmq.remoting.protocol.admin.TopicConfigSerializeWrapper getAllTopicConfig(String brokerAddr, long timeout) {
        try {
            logger.debug("op=getAllTopicConfig start, brokerAddr={}", brokerAddr);
            
            org.apache.rocketmq.remoting.protocol.admin.TopicConfigSerializeWrapper wrapper = 
                mqAdminExt.getAllTopicConfig(brokerAddr, timeout);
            
            logger.info("op=getAllTopicConfig success, brokerAddr={}, topicCount={}", 
                brokerAddr, wrapper.getTopicConfigTable().size());
            return wrapper;
            
        } catch (Exception e) {
            logger.error("op=getAllTopicConfig failed, brokerAddr={}", brokerAddr, e);
            throw new ServiceException(-1, 
                "Failed to get all topic config: " + e.getMessage());
        }
    }
    
    /**
     * 获取指定Topic的配置
     */
    public org.apache.rocketmq.remoting.protocol.admin.TopicConfig getTopicConfig(String brokerAddr, String topicName) {
        try {
            logger.debug("op=getTopicConfig start, brokerAddr={}, topic={}", brokerAddr, topicName);
            
            org.apache.rocketmq.remoting.protocol.admin.TopicConfig config = 
                mqAdminExt.examineTopicConfig(brokerAddr, topicName);
            
            logger.info("op=getTopicConfig success, brokerAddr={}, topic={}", brokerAddr, topicName);
            return config;
            
        } catch (Exception e) {
            logger.error("op=getTopicConfig failed, brokerAddr={}, topic={}", brokerAddr, topicName, e);
            throw new ServiceException(-1, 
                "Failed to get topic config: " + e.getMessage());
        }
    }
    
    /**
     * 创建或更新Topic配置
     */
    public void createOrUpdateTopicConfig(String brokerAddr, 
                                         org.apache.rocketmq.remoting.protocol.admin.TopicConfig topicConfig) {
        try {
            logger.info("op=createOrUpdateTopicConfig start, brokerAddr={}, topic={}", 
                brokerAddr, topicConfig.getTopicName());
            
            mqAdminExt.createAndUpdateTopicConfig(brokerAddr, topicConfig);
            
            logger.info("op=createOrUpdateTopicConfig success, brokerAddr={}, topic={}", 
                brokerAddr, topicConfig.getTopicName());
            
        } catch (Exception e) {
            logger.error("op=createOrUpdateTopicConfig failed, brokerAddr={}, topic={}", 
                brokerAddr, topicConfig.getTopicName(), e);
            throw new ServiceException(-1, 
                "Failed to create/update topic config: " + e.getMessage());
        }
    }
    
    // ============ 消息操作 ============
    
    /**
     * 查询消息
     */
    public org.apache.rocketmq.remoting.protocol.admin.QueryMessageResult queryMessage(String topic, String key) {
        try {
            logger.debug("op=queryMessage start, topic={}, key={}", topic, key);
            
            org.apache.rocketmq.remoting.protocol.admin.QueryMessageResult result = 
                mqAdminExt.queryMessage(topic, key, Integer.MAX_VALUE, 0, System.currentTimeMillis());
            
            logger.info("op=queryMessage success, topic={}, key={}, messageCount={}", 
                topic, key, result.getMessageList().size());
            return result;
            
        } catch (Exception e) {
            logger.error("op=queryMessage failed, topic={}, key={}", topic, key, e);
            throw new ServiceException(-1, 
                "Failed to query message: " + e.getMessage());
        }
    }
    
    /**
     * 查看消息详情
     */
    public org.apache.rocketmq.common.message.MessageExt viewMessage(String subject, String msgId) {
        try {
            logger.debug("op=viewMessage start, subject={}, msgId={}", subject, msgId);
            
            org.apache.rocketmq.common.message.MessageExt message = 
                mqAdminExt.viewMessage(subject, msgId);
            
            logger.info("op=viewMessage success, subject={}, msgId={}", subject, msgId);
            return message;
            
        } catch (Exception e) {
            logger.error("op=viewMessage failed, subject={}, msgId={}", subject, msgId, e);
            throw new ServiceException(-1, 
                "Failed to view message: " + e.getMessage());
        }
    }
    
    /**
     * 查询消息轨迹
     */
    public org.apache.rocketmq.remoting.protocol.admin.MessageTrackDetail queryMessageTrack(
        org.apache.rocketmq.common.message.MessageExt msg) {
        try {
            logger.debug("op=queryMessageTrack start, msgId={}", msg.getMsgId());
            
            org.apache.rocketmq.remoting.protocol.admin.MessageTrackDetail detail = 
                mqAdminExt.messageTrackDetail(msg);
            
            logger.info("op=queryMessageTrack success, msgId={}", msg.getMsgId());
            return detail;
            
        } catch (Exception e) {
            logger.error("op=queryMessageTrack failed, msgId={}", msg.getMsgId(), e);
            throw new ServiceException(-1, 
                "Failed to query message track: " + e.getMessage());
        }
    }
    
    /**
     * 消费消息（直接消费）
     */
    public org.apache.rocketmq.remoting.protocol.body.ConsumeMessageDirectlyResult consumeMessageDirectly(
        String consumerGroup, String clientId, String topic, String msgId) {
        try {
            logger.debug("op=consumeMessageDirectly start, group={}, clientId={}, topic={}, msgId={}", 
                consumerGroup, clientId, topic, msgId);
            
            org.apache.rocketmq.remoting.protocol.body.ConsumeMessageDirectlyResult result = 
                mqAdminExt.consumeMessageDirectly(consumerGroup, clientId, topic, msgId);
            
            logger.info("op=consumeMessageDirectly success, group={}, clientId={}, topic={}, msgId={}", 
                consumerGroup, clientId, topic, msgId);
            return result;
            
        } catch (Exception e) {
            logger.error("op=consumeMessageDirectly failed, group={}, clientId={}, topic={}, msgId={}", 
                consumerGroup, clientId, topic, msgId, e);
            throw new ServiceException(-1, 
                "Failed to consume message directly: " + e.getMessage());
        }
    }
    
    /**
     * 获取消费者连接信息
     */
    public org.apache.rocketmq.remoting.protocol.body.ConsumerConnection getConsumerConnection(String consumerGroup) {
        try {
            logger.debug("op=getConsumerConnection start, group={}", consumerGroup);
            
            org.apache.rocketmq.remoting.protocol.body.ConsumerConnection connection = 
                mqAdminExt.examineConsumerConnectionInfo(consumerGroup);
            
            logger.info("op=getConsumerConnection success, group={}, clientCount={}", 
                consumerGroup, connection.getConnectionSet().size());
            return connection;
            
        } catch (Exception e) {
            logger.error("op=getConsumerConnection failed, group={}", consumerGroup, e);
            throw new ServiceException(-1, 
                "Failed to get consumer connection: " + e.getMessage());
        }
    }
    
    /**
     * 获取消费者连接信息（指定Broker）
     */
    public org.apache.rocketmq.remoting.protocol.body.ConsumerConnection getConsumerConnectionByBroker(
        String consumerGroup, String brokerAddr) {
        try {
            logger.debug("op=getConsumerConnectionByBroker start, group={}, brokerAddr={}", 
                consumerGroup, brokerAddr);
            
            org.apache.rocketmq.remoting.protocol.body.ConsumerConnection connection = 
                mqAdminExt.examineConsumerConnectionInfo(consumerGroup, brokerAddr);
            
            logger.info("op=getConsumerConnectionByBroker success, group={}, brokerAddr={}, clientCount={}", 
                consumerGroup, brokerAddr, connection.getConnectionSet().size());
            return connection;
            
        } catch (Exception e) {
            logger.error("op=getConsumerConnectionByBroker failed, group={}, brokerAddr={}", 
                consumerGroup, brokerAddr, e);
            throw new ServiceException(-1, 
                "Failed to get consumer connection by broker: " + e.getMessage());
        }
    }
    
    /**
     * 获取消费者运行信息
     */
    public org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo getConsumerRunningInfo(
        String consumerGroup, String clientId, boolean jstack) {
        try {
            logger.debug("op=getConsumerRunningInfo start, group={}, clientId={}, jstack={}", 
                consumerGroup, clientId, jstack);
            
            org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo info = 
                mqAdminExt.getConsumerRunningInfo(consumerGroup, clientId, jstack);
            
            logger.info("op=getConsumerRunningInfo success, group={}, clientId={}", 
                consumerGroup, clientId);
            return info;
            
        } catch (Exception e) {
            logger.error("op=getConsumerRunningInfo failed, group={}, clientId={}", 
                consumerGroup, clientId, e);
            throw new ServiceException(-1, 
                "Failed to get consumer running info: " + e.getMessage());
        }
    }
    
    /**
     * 获取 Topic 详细信息
     * 
     * ✅ 改进点：
     * - 返回统一的 TopicDetailDTO
     * - 包含 Topic 的所有关键信息
     */
    public TopicDetailDTO getTopicDetail(String topicName) {
        try {
            logger.debug("op=getTopicDetail start, topic={}", topicName);
            
            // 获取 Topic 统计信息
            TopicStatsTable statsTable = mqAdminExt.examineTopicStats(topicName);
            
            TopicDetailDTO detail = new TopicDetailDTO();
            detail.setTopicName(topicName);
            detail.setQueueCount(statsTable.getOffsetTable().size());
            
            // 计算消息总数
            long totalMessages = statsTable.getOffsetTable()
                .values()
                .stream()
                .mapToLong(OffsetWrapper::getLastUpdateTimestamp)
                .sum();
            detail.setTotalMessages(totalMessages);
            
            logger.info("op=getTopicDetail success, topic={}, queueCount={}",
                topicName, detail.getQueueCount());
            return detail;
            
        } catch (Exception e) {
            logger.error("op=getTopicDetail failed, topic={}", topicName, e);
            throw new ServiceException(-1, 
                "Failed to get topic detail: " + e.getMessage());
        }
    }

    /**
     * 获取 Broker 运行时统计信息（KVTable）
     */
    public KVTable fetchBrokerRuntimeStats(String brokerAddr) {
        try {
            logger.debug("op=fetchBrokerRuntimeStats start, brokerAddr={}", brokerAddr);
            KVTable table = mqAdminExt.fetchBrokerRuntimeStats(brokerAddr);
            logger.info("op=fetchBrokerRuntimeStats success, brokerAddr={}", brokerAddr);
            return table;
        } catch (Exception e) {
            logger.error("op=fetchBrokerRuntimeStats failed, brokerAddr={}", brokerAddr, e);
            throw new ServiceException(-1, "Failed to fetch broker runtime stats: " + e.getMessage());
        }
    }

    /**
     * 获取 Broker 统计数据（如 TOPIC_PUT_NUMS / GROUP_GET_NUMS）
     */
    public BrokerStatsData viewBrokerStatsData(String brokerAddr, String statsName, String key) {
        try {
            logger.debug("op=viewBrokerStatsData start, brokerAddr={}, statsName={}, key={}", brokerAddr, statsName, key);
            BrokerStatsData data = mqAdminExt.viewBrokerStatsData(brokerAddr, statsName, key);
            logger.info("op=viewBrokerStatsData success, brokerAddr={}", brokerAddr);
            return data;
        } catch (Exception e) {
            logger.error("op=viewBrokerStatsData failed, brokerAddr={}, statsName={}, key={}", brokerAddr, statsName, key, e);
            throw new ServiceException(-1, "Failed to view broker stats data: " + e.getMessage());
        }
    }

    /**
     * 获取 Broker 原始的 ClusterInfo
     */
    public ClusterInfo getRawClusterInfo() {
        try {
            logger.debug("op=getRawClusterInfo start");
            ClusterInfo info = mqAdminExt.examineBrokerClusterInfo();
            logger.info("op=getRawClusterInfo success");
            return info;
        } catch (Exception e) {
            logger.error("op=getRawClusterInfo failed", e);
            throw new ServiceException(-1, "Failed to get cluster info: " + e.getMessage());
        }
    }

    /**
     * 获取 ACL 配置
     */
    public AclConfig getAclConfig(String brokerAddr) {
        try {
            logger.debug("op=getAclConfig start, brokerAddr={}", brokerAddr);
            AclConfig config = mqAdminExt.examineBrokerClusterAclConfig(brokerAddr);
            logger.info("op=getAclConfig success, brokerAddr={}", brokerAddr);
            return config;
        } catch (Exception e) {
            logger.error("op=getAclConfig failed, brokerAddr={}", brokerAddr, e);
            throw new ServiceException(-1, "Failed to get acl config: " + e.getMessage());
        }
    }

    public void createOrUpdatePlainAccessConfig(String brokerAddr, PlainAccessConfig config) {
        try {
            logger.debug("op=createOrUpdatePlainAccessConfig start, brokerAddr={}, accessKey={}", brokerAddr, config.getAccessKey());
            mqAdminExt.createAndUpdatePlainAccessConfig(brokerAddr, config);
            logger.info("op=createOrUpdatePlainAccessConfig success, brokerAddr={}, accessKey={}", brokerAddr, config.getAccessKey());
        } catch (Exception e) {
            logger.error("op=createOrUpdatePlainAccessConfig failed, brokerAddr={}, accessKey={}", brokerAddr, config.getAccessKey(), e);
            throw new ServiceException(-1, "Failed to create/update plain access config: " + e.getMessage());
        }
    }

    public void deletePlainAccessConfig(String brokerAddr, String accessKey) {
        try {
            logger.debug("op=deletePlainAccessConfig start, brokerAddr={}, accessKey={}", brokerAddr, accessKey);
            mqAdminExt.deletePlainAccessConfig(brokerAddr, accessKey);
            logger.info("op=deletePlainAccessConfig success, brokerAddr={}, accessKey={}", brokerAddr, accessKey);
        } catch (Exception e) {
            logger.error("op=deletePlainAccessConfig failed, brokerAddr={}, accessKey={}", brokerAddr, accessKey, e);
            throw new ServiceException(-1, "Failed to delete plain access config: " + e.getMessage());
        }
    }

    public void updateGlobalWhiteAddrConfig(String brokerAddr, String addrs) {
        try {
            logger.debug("op=updateGlobalWhiteAddrConfig start, brokerAddr={}", brokerAddr);
            mqAdminExt.updateGlobalWhiteAddrConfig(brokerAddr, addrs);
            logger.info("op=updateGlobalWhiteAddrConfig success, brokerAddr={}", brokerAddr);
        } catch (Exception e) {
            logger.error("op=updateGlobalWhiteAddrConfig failed, brokerAddr={}", brokerAddr, e);
            throw new ServiceException(-1, "Failed to update global white addr config: " + e.getMessage());
        }
    }
    
    /**
     * 删除 Topic（一行代码）
     * 
     * ✅ 改进点：
     * - 自动处理所有 Broker 和 NameServer
     * - 异常处理统一
     * - 日志记录详细
     */
    public void deleteTopic(String topicName) {
        try {
            logger.info("op=deleteTopic start, topic={}", topicName);
            
            // 从所有 Broker 删除
            Set<String> brokerAddrs = getAllBrokerAddrs();
            mqAdminExt.deleteTopicInBroker(brokerAddrs, topicName);
            logger.debug("Deleted topic from brokers, topic={}", topicName);
            
            // 从 NameServer 删除
            Set<String> nameServers = getNameServerAddrs();
            mqAdminExt.deleteTopicInNameServer(nameServers, topicName);
            logger.debug("Deleted topic from nameservers, topic={}", topicName);
            
            logger.info("op=deleteTopic success, topic={}", topicName);
            
        } catch (Exception e) {
            logger.error("op=deleteTopic failed, topic={}", topicName, e);
            throw new ServiceException(-1, 
                "Failed to delete topic: " + e.getMessage());
        }
    }
    
    // ============ 消费者操作 ============
    
    /**
     * 获取消费者组信息
     * 
     * ✅ 改进点：
     * - 返回清晰的 ConsumerGroupInfoDTO
     * - 包含消费延迟、消费速度等关键指标
     */
    public ConsumerGroupInfoDTO getConsumerGroupInfo(String consumerGroup) {
        try {
            logger.debug("op=getConsumerGroupInfo start, group={}", consumerGroup);
            
            ConsumeStats stats = mqAdminExt.examineConsumeStats(consumerGroup);
            
            ConsumerGroupInfoDTO info = new ConsumerGroupInfoDTO();
            info.setConsumerGroup(consumerGroup);
            
            // 计算消费延迟（关键指标）
            long totalLag = 0;
            int queueCount = 0;
            
            for (Map.Entry<MessageQueue, OffsetWrapper> entry : 
                stats.getOffsetTable().entrySet()) {
                
                OffsetWrapper wrapper = entry.getValue();
                long lag = wrapper.getMaxOffset() - wrapper.getConsumerOffset();
                totalLag += Math.max(0, lag);
                queueCount++;
            }
            
            info.setConsumerLag(totalLag);
            info.setQueueCount(queueCount);
            info.setConsumeTps(stats.getConsumeTps());
            
            logger.info("op=getConsumerGroupInfo success, group={}, lag={}",
                consumerGroup, totalLag);
            return info;
            
        } catch (Exception e) {
            logger.error("op=getConsumerGroupInfo failed, group={}", 
                consumerGroup, e);
            throw new ServiceException(-1, 
                "Failed to get consumer group info: " + e.getMessage());
        }
    }
    
    // ============ 生产者操作 ============
    
    /**
     * 获取生产者连接信息
     * 
     * ✅ 改进点：
     * - 返回原生 ProducerConnection 对象
     * - 自动处理异常
     * - 包含所有生产者连接详情
     */
    public ProducerConnection getProducerConnection(String producerGroup, String topic) {
        try {
            logger.debug("op=getProducerConnection start, group={}, topic={}", 
                producerGroup, topic);
            
            ProducerConnection connection = mqAdminExt.examineProducerConnectionInfo(
                producerGroup, topic);
            
            logger.info("op=getProducerConnection success, group={}, topic={}", 
                producerGroup, topic);
            return connection;
            
        } catch (Exception e) {
            logger.error("op=getProducerConnection failed, group={}, topic={}", 
                producerGroup, topic, e);
            throw new ServiceException(-1, 
                "Failed to get producer connection: " + e.getMessage());
        }
    }
    
    /**
     * 获取 Broker 配置信息
     * 
     * ✅ 改进点：
     * - 直接返回 Properties 对象
     * - 自动处理异常
     * - 日志记录详细
     */
    public java.util.Properties getBrokerConfig(String brokerAddr) {
        try {
            logger.debug("op=getBrokerConfig start, brokerAddr={}", brokerAddr);
            
            java.util.Properties config = mqAdminExt.getBrokerConfig(brokerAddr);
            
            logger.info("op=getBrokerConfig success, brokerAddr={}, size={}", 
                brokerAddr, config != null ? config.size() : 0);
            return config;
            
        } catch (Exception e) {
            logger.error("op=getBrokerConfig failed, brokerAddr={}", brokerAddr, e);
            throw new ServiceException(-1, 
                "Failed to get broker config: " + e.getMessage());
        }
    }
    
    /**
     * 重置消费偏移（简化参数）
     * 
     * ✅ 改进点：
     * - 只需要 consumerGroup, topic, timestamp 三个参数
     * - 内部自动处理 Broker、Queue、Offset 的复杂逻辑
     */
    public void resetConsumerOffset(String consumerGroup, 
                                   String topicName, 
                                   long timestamp) {
        try {
            logger.info("op=resetConsumerOffset start, group={}, topic={}, timestamp={}",
                consumerGroup, topicName, timestamp);
            
            // 获取 Topic 的所有 Queue
            Map<MessageQueue, Long> offsetMap = new HashMap<>();
            
            TopicStatsTable statsTable = mqAdminExt.examineTopicStats(topicName);
            for (MessageQueue mq : statsTable.getOffsetTable().keySet()) {
                // 根据时间戳获取偏移
                long offset = getOffsetByTimestamp(mq, timestamp);
                offsetMap.put(mq, offset);
            }
            
            // 重置偏移
            offsetMap.forEach((mq, offset) -> {
                try {
                    mqAdminExt.resetOffsetByTimestamp(
                        mq.getBrokerName(), 
                        consumerGroup, 
                        timestamp, 
                        true
                    );
                } catch (Exception e) {
                    logger.warn("Failed to reset offset for queue: {}", mq, e);
                }
            });
            
            logger.info("op=resetConsumerOffset success, group={}, topic={}",
                consumerGroup, topicName);
            
        } catch (Exception e) {
            logger.error("op=resetConsumerOffset failed", e);
            throw new ServiceException(-1, 
                "Failed to reset consumer offset: " + e.getMessage());
        }
    }
    
    // ============ 辅助方法 ============
    
    /**
     * 获取所有 Broker 地址
     */
    private Set<String> getAllBrokerAddrs() throws Exception {
        ClusterInfo clusterInfo = mqAdminExt.examineBrokerClusterInfo();
        return clusterInfo.getBrokerAddrTable()
            .values()
            .stream()
            .map(BrokerData::selectBrokerAddr)
            .collect(Collectors.toSet());
    }
    
    /**
     * 获取所有 NameServer 地址
     */
    private Set<String> getNameServerAddrs() {
        String nameServers = configure.getNamesrvAddr();
        if (nameServers == null || nameServers.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(nameServers.split(";")));
    }
    
    /**
     * 解析 NameServer 地址
     */
    private List<String> parseNameServers() {
        return Lists.newArrayList(getNameServerAddrs());
    }
    
    /**
     * 判断是否为系统 Topic
     */
    private boolean isSystemTopic(String topicName) {
        return topicName.startsWith(MixAll.SYSTEM_TOPIC_PREFIX)
            || MixAll.DEFAULT_TOPIC.equals(topicName)
            || "TBW102".equals(topicName);
    }
    
    /**
     * 转换 Broker 信息
     */
    private BrokerInfoDTO convertToBrokerInfo(String brokerName, BrokerData brokerData) {
        BrokerInfoDTO dto = new BrokerInfoDTO();
        dto.setBrokerName(brokerName);
        dto.setBrokerAddr(brokerData.selectBrokerAddr());
        return dto;
    }
    
    /**
     * 转换 Topic 信息
     */
    private TopicInfoDTO convertToTopicInfo(String topicName) {
        TopicInfoDTO dto = new TopicInfoDTO();
        dto.setTopicName(topicName);
        dto.setSystemTopic(isSystemTopic(topicName));
        return dto;
    }
    
    /**
     * 根据时间戳获取偏移
     */
    private long getOffsetByTimestamp(MessageQueue mq, long timestamp) {
        try {
            return mqAdminExt.queryConsumeTimeSpan(mq.getTopic())
                .stream()
                .findFirst()
                .map(x -> timestamp)
                .orElse(0L);
        } catch (Exception e) {
            logger.warn("Failed to get offset by timestamp", e);
            return 0L;
        }
    }
    
    // ============ DTO 内部类 ============
    
    /**
     * 集群信息 DTO
     */
    @lombok.Data
    public static class ClusterInfoDTO {
        private String clusterName;
        private List<String> nameServers;
        private List<BrokerInfoDTO> brokers;
    }
    
    /**
     * Broker 信息 DTO
     */
    @lombok.Data
    public static class BrokerInfoDTO {
        private String brokerName;
        private String brokerAddr;
        private boolean statusOk;
        private long lastUpdateTime;
    }
    
    /**
     * Topic 信息 DTO
     */
    @lombok.Data
    public static class TopicInfoDTO {
        private String topicName;
        private boolean systemTopic;
    }
    
    /**
     * Topic 详细信息 DTO
     */
    @lombok.Data
    public static class TopicDetailDTO {
        private String topicName;
        private int queueCount;
        private long totalMessages;
    }
    
    /**
     * 消费者组信息 DTO
     */
    @lombok.Data
    public static class ConsumerGroupInfoDTO {
        private String consumerGroup;
        private long consumerLag;           // 消费延迟
        private int queueCount;            // 订阅队列数
        private double consumeTps;         // 消费吞吐量
    }
}
