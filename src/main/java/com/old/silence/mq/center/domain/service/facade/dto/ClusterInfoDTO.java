package com.old.silence.mq.center.domain.service.facade.dto;

import java.util.List;

/**
 * 集群信息 DTO
 */
public class ClusterInfoDTO {
    private String clusterName;
    private List<String> nameServers;
    private List<BrokerInfoDTO> brokers;

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

    public List<BrokerInfoDTO> getBrokers() {
        return brokers;
    }

    public void setBrokers(List<BrokerInfoDTO> brokers) {
        this.brokers = brokers;
    }
}
