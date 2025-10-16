
package com.old.silence.mq.center.domain.model.request;

import java.util.List;

public class TopicTypeList {
    private List<String> topicNameList;
    private List<String> messageTypeList;

    public List<String> getTopicNameList() {
        return topicNameList;
    }

    public void setTopicNameList(List<String> topicNameList) {
        this.topicNameList = topicNameList;
    }

    public List<String> getMessageTypeList() {
        return messageTypeList;
    }

    public void setMessageTypeList(List<String> messageTypeList) {
        this.messageTypeList = messageTypeList;
    }

    public TopicTypeList(List<String> topicNameList, List<String> messageTypeList) {
        this.topicNameList = topicNameList;
        this.messageTypeList = messageTypeList;
    }
}
