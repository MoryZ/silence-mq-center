package com.old.silence.mq.center.domain.model.dto;

import java.util.List;

/**
 * Topic 视图 DTO
 * <p>
 * 代替：复杂的 TopicStatsTable + TopicRouteData + Set<MessageQueue>
 */
public class TopicViewDTO {

    /**
     * Topic 名称
     */
    private String topicName;

    /**
     * 队列数
     */
    private int queueCount;

    /**
     * 是否为系统 Topic
     */
    private boolean systemTopic;

    /**
     * 总消息数
     */
    private long totalMessages;

    /**
     * 消息发送速率（条/秒）
     */
    private double produceTps;

    /**
     * 消息消费速率（条/秒）
     */
    private double consumeTps;

    /**
     * Topic 所在的 Broker 列表
     */
    private List<String> brokers;

    /**
     * Topic 的消费者组列表
     */
    private List<String> consumerGroups;

    /**
     * 创建时间
     */
    private long createTime;

    /**
     * 最后修改时间
     */
    private long lastUpdateTime;

    /**
     * Topic 描述信息
     */
    private String description;

    public TopicViewDTO() {
    }

    public TopicViewDTO(String topicName, int queueCount, boolean systemTopic, long totalMessages, double produceTps,
                        double consumeTps, List<String> brokers, List<String> consumerGroups, long createTime,
                        long lastUpdateTime, String description) {
        this.topicName = topicName;
        this.queueCount = queueCount;
        this.systemTopic = systemTopic;
        this.totalMessages = totalMessages;
        this.produceTps = produceTps;
        this.consumeTps = consumeTps;
        this.brokers = brokers;
        this.consumerGroups = consumerGroups;
        this.createTime = createTime;
        this.lastUpdateTime = lastUpdateTime;
        this.description = description;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public int getQueueCount() {
        return queueCount;
    }

    public void setQueueCount(int queueCount) {
        this.queueCount = queueCount;
    }

    public boolean isSystemTopic() {
        return systemTopic;
    }

    public void setSystemTopic(boolean systemTopic) {
        this.systemTopic = systemTopic;
    }

    public long getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(long totalMessages) {
        this.totalMessages = totalMessages;
    }

    public double getProduceTps() {
        return produceTps;
    }

    public void setProduceTps(double produceTps) {
        this.produceTps = produceTps;
    }

    public double getConsumeTps() {
        return consumeTps;
    }

    public void setConsumeTps(double consumeTps) {
        this.consumeTps = consumeTps;
    }

    public List<String> getBrokers() {
        return brokers;
    }

    public void setBrokers(List<String> brokers) {
        this.brokers = brokers;
    }

    public List<String> getConsumerGroups() {
        return consumerGroups;
    }

    public void setConsumerGroups(List<String> consumerGroups) {
        this.consumerGroups = consumerGroups;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static class Builder {
        private String topicName;
        private int queueCount;
        private boolean systemTopic;
        private long totalMessages;
        private double produceTps;
        private double consumeTps;
        private List<String> brokers;
        private List<String> consumerGroups;
        private long createTime;
        private long lastUpdateTime;
        private String description;

        public Builder topicName(String topicName) {
            this.topicName = topicName;
            return this;
        }

        public Builder queueCount(int queueCount) {
            this.queueCount = queueCount;
            return this;
        }

        public Builder systemTopic(boolean systemTopic) {
            this.systemTopic = systemTopic;
            return this;
        }

        public Builder totalMessages(long totalMessages) {
            this.totalMessages = totalMessages;
            return this;
        }

        public Builder produceTps(double produceTps) {
            this.produceTps = produceTps;
            return this;
        }

        public Builder consumeTps(double consumeTps) {
            this.consumeTps = consumeTps;
            return this;
        }

        public Builder brokers(List<String> brokers) {
            this.brokers = brokers;
            return this;
        }

        public Builder consumerGroups(List<String> consumerGroups) {
            this.consumerGroups = consumerGroups;
            return this;
        }

        public Builder createTime(long createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder lastUpdateTime(long lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public TopicViewDTO build() {
            return new TopicViewDTO(topicName, queueCount, systemTopic, totalMessages, produceTps, consumeTps, brokers,
                    consumerGroups, createTime, lastUpdateTime, description);
        }
    }
}
