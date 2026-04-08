package com.old.silence.mq.center.dto;

import java.util.List;

public class DeleteSubGroupRequest {
    private String groupName;
    private List<String> brokerNameList;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<String> getBrokerNameList() {
        return brokerNameList;
    }

    public void setBrokerNameList(List<String> brokerNameList) {
        this.brokerNameList = brokerNameList;
    }
}
