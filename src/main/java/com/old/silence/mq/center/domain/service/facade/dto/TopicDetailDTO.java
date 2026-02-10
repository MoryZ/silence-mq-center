package com.old.silence.mq.center.domain.service.facade.dto;

/**
 * Topic 详细信息 DTO
 */
public class TopicDetailDTO {
    private String topicName;
    private int queueCount;
    private long totalMessages;

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

    public long getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(long totalMessages) {
        this.totalMessages = totalMessages;
    }
}
