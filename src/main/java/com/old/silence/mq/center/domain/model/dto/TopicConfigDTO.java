package com.old.silence.mq.center.domain.model.dto;


/**
 * Topic 配置 DTO
 */
public class TopicConfigDTO {

    /**
     * 读队列数
     */
    private int readQueueNums;

    /**
     * 写队列数
     */
    private int writeQueueNums;

    /**
     * 权限（6 = 可读可写，4 = 只读，2 = 只写）
     */
    private int perm;

    /**
     * Topic 过滤类型
     */
    private String topicFilterType;

    public TopicConfigDTO() {
    }

    public TopicConfigDTO(int readQueueNums, int writeQueueNums, int perm, String topicFilterType) {
        this.readQueueNums = readQueueNums;
        this.writeQueueNums = writeQueueNums;
        this.perm = perm;
        this.topicFilterType = topicFilterType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getReadQueueNums() {
        return readQueueNums;
    }

    public void setReadQueueNums(int readQueueNums) {
        this.readQueueNums = readQueueNums;
    }

    public int getWriteQueueNums() {
        return writeQueueNums;
    }

    public void setWriteQueueNums(int writeQueueNums) {
        this.writeQueueNums = writeQueueNums;
    }

    public int getPerm() {
        return perm;
    }

    public void setPerm(int perm) {
        this.perm = perm;
    }

    public String getTopicFilterType() {
        return topicFilterType;
    }

    public void setTopicFilterType(String topicFilterType) {
        this.topicFilterType = topicFilterType;
    }

    public static class Builder {
        private int readQueueNums;
        private int writeQueueNums;
        private int perm;
        private String topicFilterType;

        public Builder readQueueNums(int readQueueNums) {
            this.readQueueNums = readQueueNums;
            return this;
        }

        public Builder writeQueueNums(int writeQueueNums) {
            this.writeQueueNums = writeQueueNums;
            return this;
        }

        public Builder perm(int perm) {
            this.perm = perm;
            return this;
        }

        public Builder topicFilterType(String topicFilterType) {
            this.topicFilterType = topicFilterType;
            return this;
        }

        public TopicConfigDTO build() {
            return new TopicConfigDTO(readQueueNums, writeQueueNums, perm, topicFilterType);
        }
    }
}
