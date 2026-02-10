package com.old.silence.mq.center.domain.model.dto;

import java.util.List;

/**
 * Topic 详细信息 DTO
 * <p>
 * 包含 Topic 的完整信息，包括队列、消息、消费者等
 */
public class TopicDetailDTO {

    /**
     * Topic 基本信息
     */
    private TopicViewDTO basicInfo;

    /**
     * 队列详情（按队列 ID 组织）
     */
    private List<QueueDetailDTO> queues;

    /**
     * 消费者组及其消费情况
     */
    private List<ConsumerGroupDetailDTO> consumerGroups;

    /**
     * Topic 配置信息
     */
    private TopicConfigDTO config;

    public TopicDetailDTO() {
    }

    public TopicDetailDTO(TopicViewDTO basicInfo, List<QueueDetailDTO> queues,
                          List<ConsumerGroupDetailDTO> consumerGroups, TopicConfigDTO config) {
        this.basicInfo = basicInfo;
        this.queues = queues;
        this.consumerGroups = consumerGroups;
        this.config = config;
    }

    public static Builder builder() {
        return new Builder();
    }

    public TopicViewDTO getBasicInfo() {
        return basicInfo;
    }

    public void setBasicInfo(TopicViewDTO basicInfo) {
        this.basicInfo = basicInfo;
    }

    public List<QueueDetailDTO> getQueues() {
        return queues;
    }

    public void setQueues(List<QueueDetailDTO> queues) {
        this.queues = queues;
    }

    public List<ConsumerGroupDetailDTO> getConsumerGroups() {
        return consumerGroups;
    }

    public void setConsumerGroups(List<ConsumerGroupDetailDTO> consumerGroups) {
        this.consumerGroups = consumerGroups;
    }

    public TopicConfigDTO getConfig() {
        return config;
    }

    public void setConfig(TopicConfigDTO config) {
        this.config = config;
    }

    public static class Builder {
        private TopicViewDTO basicInfo;
        private List<QueueDetailDTO> queues;
        private List<ConsumerGroupDetailDTO> consumerGroups;
        private TopicConfigDTO config;

        public Builder basicInfo(TopicViewDTO basicInfo) {
            this.basicInfo = basicInfo;
            return this;
        }

        public Builder queues(List<QueueDetailDTO> queues) {
            this.queues = queues;
            return this;
        }

        public Builder consumerGroups(List<ConsumerGroupDetailDTO> consumerGroups) {
            this.consumerGroups = consumerGroups;
            return this;
        }

        public Builder config(TopicConfigDTO config) {
            this.config = config;
            return this;
        }

        public TopicDetailDTO build() {
            return new TopicDetailDTO(basicInfo, queues, consumerGroups, config);
        }
    }
}
