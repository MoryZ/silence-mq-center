package com.old.silence.mq.center.domain.service.facade.dto;

/**
 * Broker 信息 DTO
 */
public class BrokerInfoDTO {
    private String brokerName;
    private String brokerAddr;
    private boolean statusOk;
    private long lastUpdateTime;

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

    public boolean isStatusOk() {
        return statusOk;
    }

    public void setStatusOk(boolean statusOk) {
        this.statusOk = statusOk;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
