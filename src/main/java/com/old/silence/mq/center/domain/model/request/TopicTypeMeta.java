
package com.old.silence.mq.center.domain.model.request;

public class TopicTypeMeta {
    private String topicName;
    private String messageType;

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
