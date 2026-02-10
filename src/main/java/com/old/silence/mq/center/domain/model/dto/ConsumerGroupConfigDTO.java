package com.old.silence.mq.center.domain.model.dto;


/**
 * 消费者组配置 DTO
 */
public class ConsumerGroupConfigDTO {

    /**
     * 消费模式（CLUSTERING = 集群， BROADCASTING = 广播）
     */
    private String consumeModel;

    /**
     * 消息模式（CONSUME_ACTIVELY = 主动拉取， CONSUME_PASSIVELY = 被动推送）
     */
    private String messageModel;

    /**
     * 从哪里开始消费（CONSUME_FROM_LAST_OFFSET = 最后， CONSUME_FROM_FIRST_OFFSET = 最开始）
     */
    private String fromWhere;

    /**
     * 消费线程数
     */
    private int consumeThreadMin;
    private int consumeThreadMax;

    /**
     * 是否通知消费者
     */
    private boolean notifyConsumerIdsChanged;

    public ConsumerGroupConfigDTO() {
    }

    public ConsumerGroupConfigDTO(String consumeModel, String messageModel, String fromWhere, int consumeThreadMin,
                                  int consumeThreadMax, boolean notifyConsumerIdsChanged) {
        this.consumeModel = consumeModel;
        this.messageModel = messageModel;
        this.fromWhere = fromWhere;
        this.consumeThreadMin = consumeThreadMin;
        this.consumeThreadMax = consumeThreadMax;
        this.notifyConsumerIdsChanged = notifyConsumerIdsChanged;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getConsumeModel() {
        return consumeModel;
    }

    public void setConsumeModel(String consumeModel) {
        this.consumeModel = consumeModel;
    }

    public String getMessageModel() {
        return messageModel;
    }

    public void setMessageModel(String messageModel) {
        this.messageModel = messageModel;
    }

    public String getFromWhere() {
        return fromWhere;
    }

    public void setFromWhere(String fromWhere) {
        this.fromWhere = fromWhere;
    }

    public int getConsumeThreadMin() {
        return consumeThreadMin;
    }

    public void setConsumeThreadMin(int consumeThreadMin) {
        this.consumeThreadMin = consumeThreadMin;
    }

    public int getConsumeThreadMax() {
        return consumeThreadMax;
    }

    public void setConsumeThreadMax(int consumeThreadMax) {
        this.consumeThreadMax = consumeThreadMax;
    }

    public boolean isNotifyConsumerIdsChanged() {
        return notifyConsumerIdsChanged;
    }

    public void setNotifyConsumerIdsChanged(boolean notifyConsumerIdsChanged) {
        this.notifyConsumerIdsChanged = notifyConsumerIdsChanged;
    }

    public static class Builder {
        private String consumeModel;
        private String messageModel;
        private String fromWhere;
        private int consumeThreadMin;
        private int consumeThreadMax;
        private boolean notifyConsumerIdsChanged;

        public Builder consumeModel(String consumeModel) {
            this.consumeModel = consumeModel;
            return this;
        }

        public Builder messageModel(String messageModel) {
            this.messageModel = messageModel;
            return this;
        }

        public Builder fromWhere(String fromWhere) {
            this.fromWhere = fromWhere;
            return this;
        }

        public Builder consumeThreadMin(int consumeThreadMin) {
            this.consumeThreadMin = consumeThreadMin;
            return this;
        }

        public Builder consumeThreadMax(int consumeThreadMax) {
            this.consumeThreadMax = consumeThreadMax;
            return this;
        }

        public Builder notifyConsumerIdsChanged(boolean notifyConsumerIdsChanged) {
            this.notifyConsumerIdsChanged = notifyConsumerIdsChanged;
            return this;
        }

        public ConsumerGroupConfigDTO build() {
            return new ConsumerGroupConfigDTO(consumeModel, messageModel, fromWhere, consumeThreadMin,
                    consumeThreadMax, notifyConsumerIdsChanged);
        }
    }
}
