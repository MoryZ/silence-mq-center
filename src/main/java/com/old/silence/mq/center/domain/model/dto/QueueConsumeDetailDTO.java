package com.old.silence.mq.center.domain.model.dto;


/**
 * 队列消费详情 DTO
 */
public class QueueConsumeDetailDTO {

    /**
     * 队列 ID
     */
    private int queueId;

    /**
     * 消息最大偏移
     */
    private long maxOffset;

    /**
     * 消费者当前偏移
     */
    private long consumerOffset;

    /**
     * 消费延迟（条）
     */
    private long lag;

    /**
     * 最后消费时间戳
     */
    private long lastConsumeTime;

    public QueueConsumeDetailDTO() {
    }

    public QueueConsumeDetailDTO(int queueId, long maxOffset, long consumerOffset, long lag, long lastConsumeTime) {
        this.queueId = queueId;
        this.maxOffset = maxOffset;
        this.consumerOffset = consumerOffset;
        this.lag = lag;
        this.lastConsumeTime = lastConsumeTime;
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

    public long getMaxOffset() {
        return maxOffset;
    }

    public void setMaxOffset(long maxOffset) {
        this.maxOffset = maxOffset;
    }

    public long getConsumerOffset() {
        return consumerOffset;
    }

    public void setConsumerOffset(long consumerOffset) {
        this.consumerOffset = consumerOffset;
    }

    public long getLag() {
        return lag;
    }

    public void setLag(long lag) {
        this.lag = lag;
    }

    public long getLastConsumeTime() {
        return lastConsumeTime;
    }

    public void setLastConsumeTime(long lastConsumeTime) {
        this.lastConsumeTime = lastConsumeTime;
    }

    public static class Builder {
        private int queueId;
        private long maxOffset;
        private long consumerOffset;
        private long lag;
        private long lastConsumeTime;

        public Builder queueId(int queueId) {
            this.queueId = queueId;
            return this;
        }

        public Builder maxOffset(long maxOffset) {
            this.maxOffset = maxOffset;
            return this;
        }

        public Builder consumerOffset(long consumerOffset) {
            this.consumerOffset = consumerOffset;
            return this;
        }

        public Builder lag(long lag) {
            this.lag = lag;
            return this;
        }

        public Builder lastConsumeTime(long lastConsumeTime) {
            this.lastConsumeTime = lastConsumeTime;
            return this;
        }

        public QueueConsumeDetailDTO build() {
            return new QueueConsumeDetailDTO(queueId, maxOffset, consumerOffset, lag, lastConsumeTime);
        }
    }
}
