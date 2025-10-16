
package com.old.silence.mq.center.domain.model.trace;


import java.util.List;

public class SubscriptionNode {
    private String subscriptionGroup;
    private List<TraceNode> consumeNodeList;

    public String getSubscriptionGroup() {
        return subscriptionGroup;
    }

    public void setSubscriptionGroup(String subscriptionGroup) {
        this.subscriptionGroup = subscriptionGroup;
    }

    public List<TraceNode> getConsumeNodeList() {
        return consumeNodeList;
    }

    public void setConsumeNodeList(List<TraceNode> consumeNodeList) {
        this.consumeNodeList = consumeNodeList;
    }
}
