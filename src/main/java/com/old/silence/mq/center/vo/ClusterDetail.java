package com.old.silence.mq.center.vo;

import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;

import java.util.List;
import java.util.Map;

/**
 * @author moryzang
 */
public class ClusterDetail {
    private ClusterInfo clusterInfo;
    private Map<String, Map<Long, Object>> brokerServer;
    private List<String> messageTypes;

    public ClusterInfo getClusterInfo() {
        return clusterInfo;
    }

    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    public Map<String, Map<Long, Object>> getBrokerServer() {
        return brokerServer;
    }

    public void setBrokerServer(Map<String, Map<Long, Object>> brokerServer) {
        this.brokerServer = brokerServer;
    }

    public List<String> getMessageTypes() {
        return messageTypes;
    }

    public void setMessageTypes(List<String> messageTypes) {
        this.messageTypes = messageTypes;
    }
}
