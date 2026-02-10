package com.old.silence.mq.center.domain.model;


import java.util.ArrayList;
import java.util.List;

public class TopicConsumerInfo {
    private String topic;
    private long diffTotal;
    private long lastTimestamp;
    private final List<QueueStatInfo> queueStatInfoList = new ArrayList<>();

    public TopicConsumerInfo(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getDiffTotal() {
        return diffTotal;
    }

    public void setDiffTotal(long diffTotal) {
        this.diffTotal = diffTotal;
    }

    public List<QueueStatInfo> getQueueStatInfoList() {
        return queueStatInfoList;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }

    public void appendQueueStatInfo(QueueStatInfo queueStatInfo) {
        queueStatInfoList.add(queueStatInfo);
        diffTotal = diffTotal + (queueStatInfo.getBrokerOffset() - queueStatInfo.getConsumerOffset());
        lastTimestamp = Math.max(lastTimestamp, queueStatInfo.getLastTimestamp());
    }
}
