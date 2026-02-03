package com.old.silence.mq.center.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * RocketMQ 统一 DTO 层
 * 
 * 目的：
 * 1. 隐藏 Admin API 的复杂嵌套结构
 * 2. 提供清晰、易用的数据模型
 * 3. 作为 Service 层和 Controller 层之间的契约
 * 4. 易于 JSON 序列化和 API 返回
 * 
 * 设计原则：
 * - DTO 只包含必要的字段（而不是 Admin API 返回的所有字段）
 * - 字段名清晰易懂（避免 Admin API 的晦涩名称）
 * - 提供便捷的工厂方法用于构造
 */

/**
 * 集群视图 DTO
 * 
 * 代替：复杂的 ClusterInfo + BrokerData + BrokerLiveInfo 嵌套结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClusterViewDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 集群名称 */
    private String clusterName;
    
    /** NameServer 列表 */
    private List<String> nameServers;
    
    /** Broker 列表 */
    private List<BrokerViewDTO> brokers;
    
    /** 集群总消息数 */
    private long totalMessages;
    
    /** 集群在线状态 */
    private boolean online;
    
    /** 最后更新时间 */
    private long lastUpdateTime;
}


/**
 * Broker 视图 DTO
 * 
 * 代替：复杂的 BrokerData + BrokerLiveInfo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrokerViewDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** Broker 名称 */
    private String brokerName;
    
    /** Broker 地址（IP:Port） */
    private String brokerAddr;
    
    /** Master 地址（如果是 Broker Slave） */
    private String masterAddr;
    
    /** Broker ID（0 = Master, > 0 = Slave） */
    private int brokerId;
    
    /** 是否在线 */
    private boolean online;
    
    /** 总存储空间（字节） */
    private long totalStorageSize;
    
    /** 已用存储空间（字节） */
    private long usedStorageSize;
    
    /** 存储利用率（百分比） */
    private double storageUtilization;
    
    /** 最后更新时间戳 */
    private long lastUpdateTime;
}


/**
 * Topic 视图 DTO
 * 
 * 代替：复杂的 TopicStatsTable + TopicRouteData + Set<MessageQueue>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicViewDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** Topic 名称 */
    private String topicName;
    
    /** 队列数 */
    private int queueCount;
    
    /** 是否为系统 Topic */
    private boolean systemTopic;
    
    /** 总消息数 */
    private long totalMessages;
    
    /** 消息发送速率（条/秒） */
    private double produceTps;
    
    /** 消息消费速率（条/秒） */
    private double consumeTps;
    
    /** Topic 所在的 Broker 列表 */
    private List<String> brokers;
    
    /** Topic 的消费者组列表 */
    private List<String> consumerGroups;
    
    /** 创建时间 */
    private long createTime;
    
    /** 最后修改时间 */
    private long lastUpdateTime;
    
    /** Topic 描述信息 */
    private String description;
}


/**
 * Topic 详细信息 DTO
 * 
 * 包含 Topic 的完整信息，包括队列、消息、消费者等
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicDetailDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** Topic 基本信息 */
    private TopicViewDTO basicInfo;
    
    /** 队列详情（按队列 ID 组织） */
    private List<QueueDetailDTO> queues;
    
    /** 消费者组及其消费情况 */
    private List<ConsumerGroupDetailDTO> consumerGroups;
    
    /** Topic 配置信息 */
    private TopicConfigDTO config;
}


/**
 * 队列详情 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueDetailDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 队列 ID */
    private int queueId;
    
    /** 所属 Broker 名称 */
    private String brokerName;
    
    /** 最小偏移 */
    private long minOffset;
    
    /** 最大偏移（消息数） */
    private long maxOffset;
    
    /** 消息总数 */
    private long messageCount;
    
    /** 队列大小（字节） */
    private long queueSize;
}


/**
 * Topic 配置 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 读队列数 */
    private int readQueueNums;
    
    /** 写队列数 */
    private int writeQueueNums;
    
    /** 权限（6 = 可读可写，4 = 只读，2 = 只写） */
    private int perm;
    
    /** Topic 过滤类型 */
    private String topicFilterType;
}


/**
 * 消费者组视图 DTO
 * 
 * 代替：复杂的 ConsumeStats + OffsetWrapper
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumerGroupViewDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 消费者组名称 */
    private String consumerGroup;
    
    /** 订阅的 Topic 列表 */
    private List<String> topics;
    
    /** 消费者数量 */
    private int consumerCount;
    
    /** 总消费延迟（条） */
    private long totalLag;
    
    /** 消费速率（条/秒） */
    private double consumeTps;
    
    /** 消费者类型（push / pull） */
    private String consumeType;
    
    /** 最后更新时间 */
    private long lastUpdateTime;
}


/**
 * 消费者组详细信息 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumerGroupDetailDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 消费者组基本信息 */
    private ConsumerGroupViewDTO basicInfo;
    
    /** 消费成员列表 */
    private List<ConsumerMemberDTO> members;
    
    /** 按 Topic 分组的消费详情 */
    private List<TopicConsumeDetailDTO> topicConsumes;
    
    /** 消费者组配置 */
    private ConsumerGroupConfigDTO config;
}


/**
 * 消费成员 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumerMemberDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 消费者 ID */
    private String consumerId;
    
    /** 消费者 IP */
    private String clientIp;
    
    /** 消费者所属主机 */
    private String hostname;
    
    /** 版本 */
    private String version;
    
    /** 是否在线 */
    private boolean online;
    
    /** 订阅的队列列表 */
    private List<Integer> subscribedQueues;
}


/**
 * Topic 消费详情 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicConsumeDetailDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** Topic 名称 */
    private String topicName;
    
    /** 订阅队列数 */
    private int queueCount;
    
    /** 总消费延迟 */
    private long totalLag;
    
    /** 按队列的消费详情 */
    private Map<Integer, QueueConsumeDetailDTO> queueDetails;
}


/**
 * 队列消费详情 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueConsumeDetailDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 队列 ID */
    private int queueId;
    
    /** 消息最大偏移 */
    private long maxOffset;
    
    /** 消费者当前偏移 */
    private long consumerOffset;
    
    /** 消费延迟（条） */
    private long lag;
    
    /** 最后消费时间戳 */
    private long lastConsumeTime;
}


/**
 * 消费者组配置 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumerGroupConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 消费模式（CLUSTERING = 集群， BROADCASTING = 广播） */
    private String consumeModel;
    
    /** 消息模式（CONSUME_ACTIVELY = 主动拉取， CONSUME_PASSIVELY = 被动推送） */
    private String messageModel;
    
    /** 从哪里开始消费（CONSUME_FROM_LAST_OFFSET = 最后， CONSUME_FROM_FIRST_OFFSET = 最开始） */
    private String fromWhere;
    
    /** 消费线程数 */
    private int consumeThreadMin;
    private int consumeThreadMax;
    
    /** 是否通知消费者 */
    private boolean notifyConsumerIdsChanged;
}


/**
 * 消息查询结果 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageQueryResultDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 消息列表 */
    private List<MessageViewDTO> messages;
    
    /** 总数 */
    private long totalCount;
    
    /** 当前页 */
    private int currentPage;
    
    /** 页大小 */
    private int pageSize;
    
    /** 总页数 */
    private int totalPages;
}


/**
 * 消息视图 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageViewDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 消息 ID */
    private String messageId;
    
    /** 所属 Topic */
    private String topic;
    
    /** 所属队列 */
    private int queueId;
    
    /** 消息在队列中的偏移 */
    private long offset;
    
    /** 消息体（可能被截断） */
    private String body;
    
    /** 消息体长度 */
    private int bodyLength;
    
    /** 发送时间 */
    private long sendTime;
    
    /** 消息标签 */
    private String tags;
    
    /** 消息键 */
    private String keys;
    
    /** 消息属性 */
    private Map<String, String> properties;
}


/**
 * 监控数据 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonitorDataDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 时间戳 */
    private long timestamp;
    
    /** 集群指标 */
    private ClusterMetricsDTO clusterMetrics;
    
    /** Topic 指标 */
    private List<TopicMetricsDTO> topicMetrics;
    
    /** 消费者组指标 */
    private List<ConsumerGroupMetricsDTO> consumerGroupMetrics;
}


/**
 * 集群指标 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClusterMetricsDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 在线 Broker 数 */
    private int onlineBrokerCount;
    
    /** 总消息数 */
    private long totalMessages;
    
    /** 消息生产速率 */
    private double produceTps;
    
    /** 消息消费速率 */
    private double consumeTps;
    
    /** 平均消费延迟 */
    private long avgConsumerLag;
    
    /** CPU 使用率 */
    private double cpuUsage;
    
    /** 内存使用率 */
    private double memoryUsage;
}


/**
 * Topic 指标 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicMetricsDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String topicName;
    private long totalMessages;
    private double produceTps;
    private double consumeTps;
    private int consumerGroupCount;
}


/**
 * 消费者组指标 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumerGroupMetricsDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String consumerGroup;
    private long totalLag;
    private double consumeTps;
    private int consumerCount;
}
