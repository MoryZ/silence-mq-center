
package com.old.silence.mq.center.domain.model.request;

import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;

import java.util.List;

public class ConsumerConfigInfo {
    private List<String> clusterNameList;

    private List<String> brokerNameList;
    private SubscriptionGroupConfig subscriptionGroupConfig;

    public ConsumerConfigInfo() {
    }

    public ConsumerConfigInfo(List<String> brokerNameList, SubscriptionGroupConfig subscriptionGroupConfig) {
        this.brokerNameList = brokerNameList;
        this.subscriptionGroupConfig = subscriptionGroupConfig;
    }

    public List<String> getClusterNameList() {
        return clusterNameList;
    }

    public void setClusterNameList(List<String> clusterNameList) {
        this.clusterNameList = clusterNameList;
    }

    public List<String> getBrokerNameList() {
        return brokerNameList;
    }

    public void setBrokerNameList(List<String> brokerNameList) {
        this.brokerNameList = brokerNameList;
    }

    public SubscriptionGroupConfig getSubscriptionGroupConfig() {
        return subscriptionGroupConfig;
    }

    public void setSubscriptionGroupConfig(SubscriptionGroupConfig subscriptionGroupConfig) {
        this.subscriptionGroupConfig = subscriptionGroupConfig;
    }

}
