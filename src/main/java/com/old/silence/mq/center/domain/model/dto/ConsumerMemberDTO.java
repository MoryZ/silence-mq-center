package com.old.silence.mq.center.domain.model.dto;

import java.util.List;

/**
 * 消费成员 DTO
 */
public class ConsumerMemberDTO {

    /**
     * 消费者 ID
     */
    private String consumerId;

    /**
     * 消费者 IP
     */
    private String clientIp;

    /**
     * 消费者所属主机
     */
    private String hostname;

    /**
     * 版本
     */
    private String version;

    /**
     * 是否在线
     */
    private boolean online;

    /**
     * 订阅的队列列表
     */
    private List<Integer> subscribedQueues;

    public ConsumerMemberDTO() {
    }

    public ConsumerMemberDTO(String consumerId, String clientIp, String hostname, String version, boolean online,
                             List<Integer> subscribedQueues) {
        this.consumerId = consumerId;
        this.clientIp = clientIp;
        this.hostname = hostname;
        this.version = version;
        this.online = online;
        this.subscribedQueues = subscribedQueues;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public List<Integer> getSubscribedQueues() {
        return subscribedQueues;
    }

    public void setSubscribedQueues(List<Integer> subscribedQueues) {
        this.subscribedQueues = subscribedQueues;
    }

    public static class Builder {
        private String consumerId;
        private String clientIp;
        private String hostname;
        private String version;
        private boolean online;
        private List<Integer> subscribedQueues;

        public Builder consumerId(String consumerId) {
            this.consumerId = consumerId;
            return this;
        }

        public Builder clientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder online(boolean online) {
            this.online = online;
            return this;
        }

        public Builder subscribedQueues(List<Integer> subscribedQueues) {
            this.subscribedQueues = subscribedQueues;
            return this;
        }

        public ConsumerMemberDTO build() {
            return new ConsumerMemberDTO(consumerId, clientIp, hostname, version, online, subscribedQueues);
        }
    }
}
