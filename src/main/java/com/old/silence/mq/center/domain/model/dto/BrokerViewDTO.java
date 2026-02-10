package com.old.silence.mq.center.domain.model.dto;


/**
 * Broker 视图 DTO
 * <p>
 * 代替：复杂的 BrokerData + BrokerLiveInfo
 */
public class BrokerViewDTO {

    /**
     * Broker 名称
     */
    private String brokerName;

    /**
     * Broker 地址（IP:Port）
     */
    private String brokerAddr;

    /**
     * Master 地址（如果是 Broker Slave）
     */
    private String masterAddr;

    /**
     * Broker ID（0 = Master, > 0 = Slave）
     */
    private int brokerId;

    /**
     * 是否在线
     */
    private boolean online;

    /**
     * 总存储空间（字节）
     */
    private long totalStorageSize;

    /**
     * 已用存储空间（字节）
     */
    private long usedStorageSize;

    /**
     * 存储利用率（百分比）
     */
    private double storageUtilization;

    /**
     * 最后更新时间戳
     */
    private long lastUpdateTime;

    public BrokerViewDTO() {
    }

    public BrokerViewDTO(String brokerName, String brokerAddr, String masterAddr, int brokerId, boolean online,
                         long totalStorageSize, long usedStorageSize, double storageUtilization, long lastUpdateTime) {
        this.brokerName = brokerName;
        this.brokerAddr = brokerAddr;
        this.masterAddr = masterAddr;
        this.brokerId = brokerId;
        this.online = online;
        this.totalStorageSize = totalStorageSize;
        this.usedStorageSize = usedStorageSize;
        this.storageUtilization = storageUtilization;
        this.lastUpdateTime = lastUpdateTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public String getBrokerAddr() {
        return brokerAddr;
    }

    public void setBrokerAddr(String brokerAddr) {
        this.brokerAddr = brokerAddr;
    }

    public String getMasterAddr() {
        return masterAddr;
    }

    public void setMasterAddr(String masterAddr) {
        this.masterAddr = masterAddr;
    }

    public int getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(int brokerId) {
        this.brokerId = brokerId;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public long getTotalStorageSize() {
        return totalStorageSize;
    }

    public void setTotalStorageSize(long totalStorageSize) {
        this.totalStorageSize = totalStorageSize;
    }

    public long getUsedStorageSize() {
        return usedStorageSize;
    }

    public void setUsedStorageSize(long usedStorageSize) {
        this.usedStorageSize = usedStorageSize;
    }

    public double getStorageUtilization() {
        return storageUtilization;
    }

    public void setStorageUtilization(double storageUtilization) {
        this.storageUtilization = storageUtilization;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public static class Builder {
        private String brokerName;
        private String brokerAddr;
        private String masterAddr;
        private int brokerId;
        private boolean online;
        private long totalStorageSize;
        private long usedStorageSize;
        private double storageUtilization;
        private long lastUpdateTime;

        public Builder brokerName(String brokerName) {
            this.brokerName = brokerName;
            return this;
        }

        public Builder brokerAddr(String brokerAddr) {
            this.brokerAddr = brokerAddr;
            return this;
        }

        public Builder masterAddr(String masterAddr) {
            this.masterAddr = masterAddr;
            return this;
        }

        public Builder brokerId(int brokerId) {
            this.brokerId = brokerId;
            return this;
        }

        public Builder online(boolean online) {
            this.online = online;
            return this;
        }

        public Builder totalStorageSize(long totalStorageSize) {
            this.totalStorageSize = totalStorageSize;
            return this;
        }

        public Builder usedStorageSize(long usedStorageSize) {
            this.usedStorageSize = usedStorageSize;
            return this;
        }

        public Builder storageUtilization(double storageUtilization) {
            this.storageUtilization = storageUtilization;
            return this;
        }

        public Builder lastUpdateTime(long lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
            return this;
        }

        public BrokerViewDTO build() {
            return new BrokerViewDTO(brokerName, brokerAddr, masterAddr, brokerId, online, totalStorageSize,
                    usedStorageSize, storageUtilization, lastUpdateTime);
        }
    }
}
