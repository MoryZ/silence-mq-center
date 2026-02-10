package com.old.silence.mq.center.domain.model.dto;

import java.util.List;

/**
 * 消费者组详细信息 DTO
 */
public class ConsumerGroupDetailDTO {

    /**
     * 消费者组基本信息
     */
    private ConsumerGroupViewDTO basicInfo;

    /**
     * 消费成员列表
     */
    private List<ConsumerMemberDTO> members;

    /**
     * 按 Topic 分组的消费详情
     */
    private List<TopicConsumeDetailDTO> topicConsumes;

    /**
     * 消费者组配置
     */
    private ConsumerGroupConfigDTO config;

    public ConsumerGroupDetailDTO() {
    }

    public ConsumerGroupDetailDTO(ConsumerGroupViewDTO basicInfo, List<ConsumerMemberDTO> members,
                                  List<TopicConsumeDetailDTO> topicConsumes, ConsumerGroupConfigDTO config) {
        this.basicInfo = basicInfo;
        this.members = members;
        this.topicConsumes = topicConsumes;
        this.config = config;
    }

    public static Builder builder() {
        return new Builder();
    }

    public ConsumerGroupViewDTO getBasicInfo() {
        return basicInfo;
    }

    public void setBasicInfo(ConsumerGroupViewDTO basicInfo) {
        this.basicInfo = basicInfo;
    }

    public List<ConsumerMemberDTO> getMembers() {
        return members;
    }

    public void setMembers(List<ConsumerMemberDTO> members) {
        this.members = members;
    }

    public List<TopicConsumeDetailDTO> getTopicConsumes() {
        return topicConsumes;
    }

    public void setTopicConsumes(List<TopicConsumeDetailDTO> topicConsumes) {
        this.topicConsumes = topicConsumes;
    }

    public ConsumerGroupConfigDTO getConfig() {
        return config;
    }

    public void setConfig(ConsumerGroupConfigDTO config) {
        this.config = config;
    }

    public static class Builder {
        private ConsumerGroupViewDTO basicInfo;
        private List<ConsumerMemberDTO> members;
        private List<TopicConsumeDetailDTO> topicConsumes;
        private ConsumerGroupConfigDTO config;

        public Builder basicInfo(ConsumerGroupViewDTO basicInfo) {
            this.basicInfo = basicInfo;
            return this;
        }

        public Builder members(List<ConsumerMemberDTO> members) {
            this.members = members;
            return this;
        }

        public Builder topicConsumes(List<TopicConsumeDetailDTO> topicConsumes) {
            this.topicConsumes = topicConsumes;
            return this;
        }

        public Builder config(ConsumerGroupConfigDTO config) {
            this.config = config;
            return this;
        }

        public ConsumerGroupDetailDTO build() {
            return new ConsumerGroupDetailDTO(basicInfo, members, topicConsumes, config);
        }
    }
}
