package com.old.silence.mq.center.dto;

import com.old.silence.mq.center.enums.MessageType;
import com.old.silence.mq.center.enums.TopicStatus;

/**
 * @author moryzang
 */
public class TopicCommand {

    private String topicName;

    private int readQueueNums;

    private int writeQueueNums;

    /**
     * 消息类型 NORMAL, FIFO, DELAY, TRANSACTION
     */
    private MessageType messageType;

    private Boolean systemTopic;

    private TopicStatus status;

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public int getReadQueueNums() {
        return readQueueNums;
    }

    public void setReadQueueNums(int readQueueNums) {
        this.readQueueNums = readQueueNums;
    }

    public int getWriteQueueNums() {
        return writeQueueNums;
    }

    public void setWriteQueueNums(int writeQueueNums) {
        this.writeQueueNums = writeQueueNums;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public Boolean getSystemTopic() {
        return systemTopic;
    }

    public void setSystemTopic(Boolean systemTopic) {
        this.systemTopic = systemTopic;
    }

    public TopicStatus getStatus() {
        return status;
    }

    public void setStatus(TopicStatus status) {
        this.status = status;
    }
}
