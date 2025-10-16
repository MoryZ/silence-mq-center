
package com.old.silence.mq.center.domain.model.trace;


import java.util.List;

public class ProducerNode {
    private String msgId;
    private String tags;
    private String keys;
    private String offSetMsgId;
    private String topic;
    private String groupName;
    private TraceNode traceNode;
    private List<TraceNode> transactionNodeList;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getKeys() {
        return keys;
    }

    public void setKeys(String keys) {
        this.keys = keys;
    }

    public String getOffSetMsgId() {
        return offSetMsgId;
    }

    public void setOffSetMsgId(String offSetMsgId) {
        this.offSetMsgId = offSetMsgId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public TraceNode getTraceNode() {
        return traceNode;
    }

    public void setTraceNode(TraceNode traceNode) {
        this.traceNode = traceNode;
    }

    public List<TraceNode> getTransactionNodeList() {
        return transactionNodeList;
    }

    public void setTransactionNodeList(List<TraceNode> transactionNodeList) {
        this.transactionNodeList = transactionNodeList;
    }
}
