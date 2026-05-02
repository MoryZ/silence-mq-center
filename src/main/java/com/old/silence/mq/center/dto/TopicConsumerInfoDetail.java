package com.old.silence.mq.center.dto;

public class TopicConsumerInfoDetail {

    private String groupName;
    private TopicConsumerInfo topicConsumerInfo;

    public TopicConsumerInfoDetail() {
    }

    public TopicConsumerInfoDetail(String groupName, TopicConsumerInfo topicConsumerInfo) {
        this.groupName = groupName;
        this.topicConsumerInfo = topicConsumerInfo;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public TopicConsumerInfo getTopicConsumerInfo() {
        return topicConsumerInfo;
    }

    public void setTopicConsumerInfo(TopicConsumerInfo topicConsumerInfo) {
        this.topicConsumerInfo = topicConsumerInfo;
    }
}
