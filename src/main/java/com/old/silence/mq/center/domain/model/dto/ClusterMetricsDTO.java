package com.old.silence.mq.center.domain.model.dto;


/**
 * 集群指标 DTO
 */
public class ClusterMetricsDTO {

    /**
     * 在线 Broker 数
     */
    private int onlineBrokerCount;

    /**
     * 总消息数
     */
    private long totalMessages;

    /**
     * 消息生产速率
     */
    private double produceTps;

    /**
     * 消息消费速率
     */
    private double consumeTps;

    /**
     * 平均消费延迟
     */
    private long avgConsumerLag;

    /**
     * CPU 使用率
     */
    private double cpuUsage;

    /**
     * 内存使用率
     */
    private double memoryUsage;

    public ClusterMetricsDTO() {
    }

    public ClusterMetricsDTO(int onlineBrokerCount, long totalMessages, double produceTps, double consumeTps,
                             long avgConsumerLag, double cpuUsage, double memoryUsage) {
        this.onlineBrokerCount = onlineBrokerCount;
        this.totalMessages = totalMessages;
        this.produceTps = produceTps;
        this.consumeTps = consumeTps;
        this.avgConsumerLag = avgConsumerLag;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getOnlineBrokerCount() {
        return onlineBrokerCount;
    }

    public void setOnlineBrokerCount(int onlineBrokerCount) {
        this.onlineBrokerCount = onlineBrokerCount;
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

    public long getAvgConsumerLag() {
        return avgConsumerLag;
    }

    public void setAvgConsumerLag(long avgConsumerLag) {
        this.avgConsumerLag = avgConsumerLag;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public static class Builder {
        private int onlineBrokerCount;
        private long totalMessages;
        private double produceTps;
        private double consumeTps;
        private long avgConsumerLag;
        private double cpuUsage;
        private double memoryUsage;

        public Builder onlineBrokerCount(int onlineBrokerCount) {
            this.onlineBrokerCount = onlineBrokerCount;
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

        public Builder avgConsumerLag(long avgConsumerLag) {
            this.avgConsumerLag = avgConsumerLag;
            return this;
        }

        public Builder cpuUsage(double cpuUsage) {
            this.cpuUsage = cpuUsage;
            return this;
        }

        public Builder memoryUsage(double memoryUsage) {
            this.memoryUsage = memoryUsage;
            return this;
        }

        public ClusterMetricsDTO build() {
            return new ClusterMetricsDTO(onlineBrokerCount, totalMessages, produceTps, consumeTps, avgConsumerLag,
                    cpuUsage, memoryUsage);
        }
    }
}
