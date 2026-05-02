package com.old.silence.mq.center.vo;

import jakarta.validation.constraints.NotEmpty;

import com.old.silence.mq.center.enums.MessageType;

import java.util.List;

/**
 * @author moryzang
 */
public class TopicConfigInfo {

    @NotEmpty
    private List<String> clusterNameList;
    @NotEmpty
    private List<String> brokerNameList;

    /**
     * topicConfig
     */
    private String topicName;
    private int writeQueueNums;
    private int readQueueNums;
    private int perm;
    private boolean order;

    private MessageType messageType;

    public List<String> getClusterNameList() {
        return clusterNameList;
    }

    public void setClusterNameList(List<String> clusterNameList) {
        this.clusterNameList = clusterNameList;
    }

    /**
     * topicConfig
     */


    public List<String> getBrokerNameList() {
        return brokerNameList;
    }

    public void setBrokerNameList(List<String> brokerNameList) {
        this.brokerNameList = brokerNameList;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public int getWriteQueueNums() {
        return writeQueueNums;
    }

    public void setWriteQueueNums(int writeQueueNums) {
        this.writeQueueNums = writeQueueNums;
    }

    public int getReadQueueNums() {
        return readQueueNums;
    }

    public void setReadQueueNums(int readQueueNums) {
        this.readQueueNums = readQueueNums;
    }

    public int getPerm() {
        return perm;
    }

    public void setPerm(int perm) {
        this.perm = perm;
    }

    public boolean isOrder() {
        return order;
    }

    public void setOrder(boolean order) {
        this.order = order;
    }


    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}
