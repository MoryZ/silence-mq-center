package com.old.silence.mq.center.domain.model.dto;

import java.util.List;

/**
 * 集群视图 DTO
 * <p>
 * 代替：复杂的 ClusterInfo + BrokerData + BrokerLiveInfo 嵌套结构
 */
public class ClusterViewDTO {

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * NameServer 列表
     */
    private List<String> nameServers;

    /**
     * Broker 列表
     */
    private List<BrokerViewDTO> brokers;

    /**
     * 集群总消息数
     */
    private long totalMessages;

    /**
     * 集群在线状态
     */
    private boolean online;

    /**
     * 最后更新时间
     */
    private long lastUpdateTime;

    public ClusterViewDTO() {
    }

    public ClusterViewDTO(String clusterName, List<String> nameServers, List<BrokerViewDTO> brokers,
                          long totalMessages, boolean online, long lastUpdateTime) {
        this.clusterName = clusterName;
        this.nameServers = nameServers;
        this.brokers = brokers;
        this.totalMessages = totalMessages;
        this.online = online;
        this.lastUpdateTime = lastUpdateTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public List<String> getNameServers() {
        return nameServers;
    }

    public void setNameServers(List<String> nameServers) {
        this.nameServers = nameServers;
    }

    public List<BrokerViewDTO> getBrokers() {
        return brokers;
    }

    public void setBrokers(List<BrokerViewDTO> brokers) {
        this.brokers = brokers;
    }

    public long getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(long totalMessages) {
        this.totalMessages = totalMessages;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public static class Builder {
        private String clusterName;
        private List<String> nameServers;
        private List<BrokerViewDTO> brokers;
        private long totalMessages;
        private boolean online;
        private long lastUpdateTime;

        public Builder clusterName(String clusterName) {
            this.clusterName = clusterName;
            return this;
        }

        public Builder nameServers(List<String> nameServers) {
            this.nameServers = nameServers;
            return this;
        }

        public Builder brokers(List<BrokerViewDTO> brokers) {
            this.brokers = brokers;
            return this;
        }

        public Builder totalMessages(long totalMessages) {
            this.totalMessages = totalMessages;
            return this;
        }

        public Builder online(boolean online) {
            this.online = online;
            return this;
        }

        public Builder lastUpdateTime(long lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
            return this;
        }

        public ClusterViewDTO build() {
            return new ClusterViewDTO(clusterName, nameServers, brokers, totalMessages, online, lastUpdateTime);
        }
    }
}
