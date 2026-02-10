package com.old.silence.mq.center.domain.model.dto;


/**
 * 消费者组指标 DTO
 */
public class ConsumerGroupMetricsDTO {

    private String consumerGroup;
    private long totalLag;
    private double consumeTps;
    private int consumerCount;

    public ConsumerGroupMetricsDTO() {
    }

    public ConsumerGroupMetricsDTO(String consumerGroup, long totalLag, double consumeTps, int consumerCount) {
        this.consumerGroup = consumerGroup;
        this.totalLag = totalLag;
        this.consumeTps = consumeTps;
        this.consumerCount = consumerCount;
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

    public int getConsumerCount() {
        return consumerCount;
    }

    public void setConsumerCount(int consumerCount) {
        this.consumerCount = consumerCount;
    }

    public static class Builder {
        private String consumerGroup;
        private long totalLag;
        private double consumeTps;
        private int consumerCount;

        public Builder consumerGroup(String consumerGroup) {
            this.consumerGroup = consumerGroup;
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

        public Builder consumerCount(int consumerCount) {
            this.consumerCount = consumerCount;
            return this;
        }

        public ConsumerGroupMetricsDTO build() {
            return new ConsumerGroupMetricsDTO(consumerGroup, totalLag, consumeTps, consumerCount);
        }
    }
}
