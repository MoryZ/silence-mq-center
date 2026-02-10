package com.old.silence.mq.center.domain.model.dto;


/**
 * 队列详情 DTO
 */
public class QueueDetailDTO {

    /**
     * 队列 ID
     */
    private int queueId;

    /**
     * 所属 Broker 名称
     */
    private String brokerName;

    /**
     * 最小偏移
     */
    private long minOffset;

    /**
     * 最大偏移（消息数）
     */
    private long maxOffset;

    /**
     * 消息总数
     */
    private long messageCount;

    /**
     * 队列大小（字节）
     */
    private long queueSize;

    public QueueDetailDTO() {
    }

    public QueueDetailDTO(int queueId, String brokerName, long minOffset, long maxOffset, long messageCount,
                          long queueSize) {
        this.queueId = queueId;
        this.brokerName = brokerName;
        this.minOffset = minOffset;
        this.maxOffset = maxOffset;
        this.messageCount = messageCount;
        this.queueSize = queueSize;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public long getMinOffset() {
        return minOffset;
    }

    public void setMinOffset(long minOffset) {
        this.minOffset = minOffset;
    }

    public long getMaxOffset() {
        return maxOffset;
    }

    public void setMaxOffset(long maxOffset) {
        this.maxOffset = maxOffset;
    }

    public long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(long messageCount) {
        this.messageCount = messageCount;
    }

    public long getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(long queueSize) {
        this.queueSize = queueSize;
    }

    public static class Builder {
        private int queueId;
        private String brokerName;
        private long minOffset;
        private long maxOffset;
        private long messageCount;
        private long queueSize;

        public Builder queueId(int queueId) {
            this.queueId = queueId;
            return this;
        }

        public Builder brokerName(String brokerName) {
            this.brokerName = brokerName;
            return this;
        }

        public Builder minOffset(long minOffset) {
            this.minOffset = minOffset;
            return this;
        }

        public Builder maxOffset(long maxOffset) {
            this.maxOffset = maxOffset;
            return this;
        }

        public Builder messageCount(long messageCount) {
            this.messageCount = messageCount;
            return this;
        }

        public Builder queueSize(long queueSize) {
            this.queueSize = queueSize;
            return this;
        }

        public QueueDetailDTO build() {
            return new QueueDetailDTO(queueId, brokerName, minOffset, maxOffset, messageCount, queueSize);
        }
    }
}
