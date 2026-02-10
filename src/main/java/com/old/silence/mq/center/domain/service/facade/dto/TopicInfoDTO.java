package com.old.silence.mq.center.domain.service.facade.dto;

/**
 * Topic 信息 DTO
 */
public class TopicInfoDTO {
    private String topicName;
    private boolean systemTopic;

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public boolean isSystemTopic() {
        return systemTopic;
    }

    public void setSystemTopic(boolean systemTopic) {
        this.systemTopic = systemTopic;
    }
}
