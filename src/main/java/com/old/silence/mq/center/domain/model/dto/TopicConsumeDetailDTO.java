package com.old.silence.mq.center.domain.model.dto;

import java.util.Map;

/**
 * Topic 消费详情 DTO
 */
public class TopicConsumeDetailDTO {

    /**
     * Topic 名称
     */
    private String topicName;

    /**
     * 订阅队列数
     */
    private int queueCount;

    /**
     * 总消费延迟
     */
    private long totalLag;

    /**
     * 按队列的消费详情
     */
    private Map<Integer, QueueConsumeDetailDTO> queueDetails;

    public TopicConsumeDetailDTO() {
    }

    public TopicConsumeDetailDTO(String topicName, int queueCount, long totalLag,
                                 Map<Integer, QueueConsumeDetailDTO> queueDetails) {
        this.topicName = topicName;
        this.queueCount = queueCount;
        this.totalLag = totalLag;
        this.queueDetails = queueDetails;
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

    public long getTotalLag() {
        return totalLag;
    }

    public void setTotalLag(long totalLag) {
        this.totalLag = totalLag;
    }

    public Map<Integer, QueueConsumeDetailDTO> getQueueDetails() {
        return queueDetails;
    }

    public void setQueueDetails(Map<Integer, QueueConsumeDetailDTO> queueDetails) {
        this.queueDetails = queueDetails;
    }

    public static class Builder {
        private String topicName;
        private int queueCount;
        private long totalLag;
        private Map<Integer, QueueConsumeDetailDTO> queueDetails;

        public Builder topicName(String topicName) {
            this.topicName = topicName;
            return this;
        }

        public Builder queueCount(int queueCount) {
            this.queueCount = queueCount;
            return this;
        }

        public Builder totalLag(long totalLag) {
            this.totalLag = totalLag;
            return this;
        }

        public Builder queueDetails(Map<Integer, QueueConsumeDetailDTO> queueDetails) {
            this.queueDetails = queueDetails;
            return this;
        }

        public TopicConsumeDetailDTO build() {
            return new TopicConsumeDetailDTO(topicName, queueCount, totalLag, queueDetails);
        }
    }
}
