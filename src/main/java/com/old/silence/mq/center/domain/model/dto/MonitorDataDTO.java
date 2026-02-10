package com.old.silence.mq.center.domain.model.dto;

import java.util.List;

/**
 * 监控数据 DTO
 */
public class MonitorDataDTO {

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 集群指标
     */
    private ClusterMetricsDTO clusterMetrics;

    /**
     * Topic 指标
     */
    private List<TopicMetricsDTO> topicMetrics;

    /**
     * 消费者组指标
     */
    private List<ConsumerGroupMetricsDTO> consumerGroupMetrics;

    public MonitorDataDTO() {
    }

    public MonitorDataDTO(long timestamp, ClusterMetricsDTO clusterMetrics, List<TopicMetricsDTO> topicMetrics,
                          List<ConsumerGroupMetricsDTO> consumerGroupMetrics) {
        this.timestamp = timestamp;
        this.clusterMetrics = clusterMetrics;
        this.topicMetrics = topicMetrics;
        this.consumerGroupMetrics = consumerGroupMetrics;
    }

    public static Builder builder() {
        return new Builder();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public ClusterMetricsDTO getClusterMetrics() {
        return clusterMetrics;
    }

    public void setClusterMetrics(ClusterMetricsDTO clusterMetrics) {
        this.clusterMetrics = clusterMetrics;
    }

    public List<TopicMetricsDTO> getTopicMetrics() {
        return topicMetrics;
    }

    public void setTopicMetrics(List<TopicMetricsDTO> topicMetrics) {
        this.topicMetrics = topicMetrics;
    }

    public List<ConsumerGroupMetricsDTO> getConsumerGroupMetrics() {
        return consumerGroupMetrics;
    }

    public void setConsumerGroupMetrics(List<ConsumerGroupMetricsDTO> consumerGroupMetrics) {
        this.consumerGroupMetrics = consumerGroupMetrics;
    }

    public static class Builder {
        private long timestamp;
        private ClusterMetricsDTO clusterMetrics;
        private List<TopicMetricsDTO> topicMetrics;
        private List<ConsumerGroupMetricsDTO> consumerGroupMetrics;

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder clusterMetrics(ClusterMetricsDTO clusterMetrics) {
            this.clusterMetrics = clusterMetrics;
            return this;
        }

        public Builder topicMetrics(List<TopicMetricsDTO> topicMetrics) {
            this.topicMetrics = topicMetrics;
            return this;
        }

        public Builder consumerGroupMetrics(List<ConsumerGroupMetricsDTO> consumerGroupMetrics) {
            this.consumerGroupMetrics = consumerGroupMetrics;
            return this;
        }

        public MonitorDataDTO build() {
            return new MonitorDataDTO(timestamp, clusterMetrics, topicMetrics, consumerGroupMetrics);
        }
    }
}
