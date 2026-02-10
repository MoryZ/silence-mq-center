package com.old.silence.mq.center.domain.model.dto;

import java.util.List;

/**
 * 消费者组视图 DTO
 * <p>
 * 代替：复杂的 ConsumeStats + OffsetWrapper
 */
public class ConsumerGroupViewDTO {

    /**
     * 消费者组名称
     */
    private String consumerGroup;

    /**
     * 订阅的 Topic 列表
     */
    private List<String> topics;

    /**
     * 消费者数量
     */
    private int consumerCount;

    /**
     * 总消费延迟（条）
     */
    private long totalLag;

    /**
     * 消费速率（条/秒）
     */
    private double consumeTps;

    /**
     * 消费者类型（push / pull）
     */
    private String consumeType;

    /**
     * 最后更新时间
     */
    private long lastUpdateTime;

    public ConsumerGroupViewDTO() {
    }

    public ConsumerGroupViewDTO(String consumerGroup, List<String> topics, int consumerCount, long totalLag,
                                double consumeTps, String consumeType, long lastUpdateTime) {
        this.consumerGroup = consumerGroup;
        this.topics = topics;
        this.consumerCount = consumerCount;
        this.totalLag = totalLag;
        this.consumeTps = consumeTps;
        this.consumeType = consumeType;
        this.lastUpdateTime = lastUpdateTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public int getConsumerCount() {
        return consumerCount;
    }

    public void setConsumerCount(int consumerCount) {
        this.consumerCount = consumerCount;
    }

    public long getTotalLag() {
        return totalLag;
    }

    public void setTotalLag(long totalLag) {
        this.totalLag = totalLag;
    }

    public double getConsumeTps() {
        return consumeTps;
    }

    public void setConsumeTps(double consumeTps) {
        this.consumeTps = consumeTps;
    }

    public String getConsumeType() {
        return consumeType;
    }

    public void setConsumeType(String consumeType) {
        this.consumeType = consumeType;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public static class Builder {
        private String consumerGroup;
        private List<String> topics;
        private int consumerCount;
        private long totalLag;
        private double consumeTps;
        private String consumeType;
        private long lastUpdateTime;

        public Builder consumerGroup(String consumerGroup) {
            this.consumerGroup = consumerGroup;
            return this;
        }

        public Builder topics(List<String> topics) {
            this.topics = topics;
            return this;
        }

        public Builder consumerCount(int consumerCount) {
            this.consumerCount = consumerCount;
            return this;
        }

        public Builder totalLag(long totalLag) {
            this.totalLag = totalLag;
            return this;
        }

        public Builder consumeTps(double consumeTps) {
            this.consumeTps = consumeTps;
            return this;
        }

        public Builder consumeType(String consumeType) {
            this.consumeType = consumeType;
            return this;
        }

        public Builder lastUpdateTime(long lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
            return this;
        }

        public ConsumerGroupViewDTO build() {
            return new ConsumerGroupViewDTO(consumerGroup, topics, consumerCount, totalLag, consumeTps, consumeType,
                    lastUpdateTime);
        }
    }
}
