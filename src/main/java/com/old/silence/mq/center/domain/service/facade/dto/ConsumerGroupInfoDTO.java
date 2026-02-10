package com.old.silence.mq.center.domain.service.facade.dto;

/**
 * 消费者组信息 DTO
 */
public class ConsumerGroupInfoDTO {
    private String consumerGroup;
    private long consumerLag;           // 消费延迟
    private int queueCount;            // 订阅队列数
    private double consumeTps;         // 消费吞吐量

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public long getConsumerLag() {
        return consumerLag;
    }

    public void setConsumerLag(long consumerLag) {
        this.consumerLag = consumerLag;
    }

    public int getQueueCount() {
        return queueCount;
    }

    public void setQueueCount(int queueCount) {
        this.queueCount = queueCount;
    }

    public double getConsumeTps() {
        return consumeTps;
    }

    public void setConsumeTps(double consumeTps) {
        this.consumeTps = consumeTps;
    }
}
