

package com.old.silence.mq.center.domain.model.trace;


import com.old.silence.mq.center.domain.model.MessageTraceView;

import java.util.List;

public class MessageTraceGraph {
    private ProducerNode producerNode;
    private List<SubscriptionNode> subscriptionNodeList;
    private List<MessageTraceView> messageTraceViews;

    public ProducerNode getProducerNode() {
        return producerNode;
    }

    public void setProducerNode(ProducerNode producerNode) {
        this.producerNode = producerNode;
    }

    public List<SubscriptionNode> getSubscriptionNodeList() {
        return subscriptionNodeList;
    }

    public void setSubscriptionNodeList(List<SubscriptionNode> subscriptionNodeList) {
        this.subscriptionNodeList = subscriptionNodeList;
    }

    public List<MessageTraceView> getMessageTraceViews() {
        return messageTraceViews;
    }

    public void setMessageTraceViews(List<MessageTraceView> messageTraceViews) {
        this.messageTraceViews = messageTraceViews;
    }
}
