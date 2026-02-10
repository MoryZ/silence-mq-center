package com.old.silence.mq.center.domain.model.dto;


/**
 * Topic 指标 DTO
 */
public class TopicMetricsDTO {

    private String topicName;
    private long totalMessages;
    private double produceTps;
    private double consumeTps;
    private int consumerGroupCount;

    public TopicMetricsDTO() {
    }

    public TopicMetricsDTO(String topicName, long totalMessages, double produceTps, double consumeTps,
                           int consumerGroupCount) {
        this.topicName = topicName;
        this.totalMessages = totalMessages;
        this.produceTps = produceTps;
        this.consumeTps = consumeTps;
        this.consumerGroupCount = consumerGroupCount;
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

    public int getConsumerGroupCount() {
        return consumerGroupCount;
    }

    public void setConsumerGroupCount(int consumerGroupCount) {
        this.consumerGroupCount = consumerGroupCount;
    }

    public static class Builder {
        private String topicName;
        private long totalMessages;
        private double produceTps;
        private double consumeTps;
        private int consumerGroupCount;

        public Builder topicName(String topicName) {
            this.topicName = topicName;
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

        public Builder consumerGroupCount(int consumerGroupCount) {
            this.consumerGroupCount = consumerGroupCount;
            return this;
        }

        public TopicMetricsDTO build() {
            return new TopicMetricsDTO(topicName, totalMessages, produceTps, consumeTps, consumerGroupCount);
        }
    }
}
