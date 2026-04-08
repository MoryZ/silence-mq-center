package com.old.silence.mq.center.dto;

/**
 * @author moryzang
 */
public class TopicCommand {

    private String clusterName;

    private String topicName;

    private int readQueueNums;

    private int writeQueueNums;

    /**
     *  消息类型 NORMAL, FIFO, DELAY, TRANSACTION
     */
    private String messageType;

    private String brokerAddr;

    private Boolean systemTopic;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

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

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getBrokerAddr() {
        return brokerAddr;
    }

    public void setBrokerAddr(String brokerAddr) {
        this.brokerAddr = brokerAddr;
    }

    public Boolean getSystemTopic() {
        return systemTopic;
    }

    public void setSystemTopic(Boolean systemTopic) {
        this.systemTopic = systemTopic;
    }
}
