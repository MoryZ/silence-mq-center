package com.old.silence.mq.center.domain.model.dto;

import java.util.Map;

/**
 * 消息视图 DTO
 */
public class MessageViewDTO {

    /**
     * 消息 ID
     */
    private String messageId;

    /**
     * 所属 Topic
     */
    private String topic;

    /**
     * 所属队列
     */
    private int queueId;

    /**
     * 消息在队列中的偏移
     */
    private long offset;

    /**
     * 消息体（可能被截断）
     */
    private String body;

    /**
     * 消息体长度
     */
    private int bodyLength;

    /**
     * 发送时间
     */
    private long sendTime;

    /**
     * 消息标签
     */
    private String tags;

    /**
     * 消息键
     */
    private String keys;

    /**
     * 消息属性
     */
    private Map<String, String> properties;

    public MessageViewDTO() {
    }

    public MessageViewDTO(String messageId, String topic, int queueId, long offset, String body, int bodyLength,
                          long sendTime, String tags, String keys, Map<String, String> properties) {
        this.messageId = messageId;
        this.topic = topic;
        this.queueId = queueId;
        this.offset = offset;
        this.body = body;
        this.bodyLength = bodyLength;
        this.sendTime = sendTime;
        this.tags = tags;
        this.keys = keys;
        this.properties = properties;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getBodyLength() {
        return bodyLength;
    }

    public void setBodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getKeys() {
        return keys;
    }

    public void setKeys(String keys) {
        this.keys = keys;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public static class Builder {
        private String messageId;
        private String topic;
        private int queueId;
        private long offset;
        private String body;
        private int bodyLength;
        private long sendTime;
        private String tags;
        private String keys;
        private Map<String, String> properties;

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public Builder queueId(int queueId) {
            this.queueId = queueId;
            return this;
        }

        public Builder offset(long offset) {
            this.offset = offset;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder bodyLength(int bodyLength) {
            this.bodyLength = bodyLength;
            return this;
        }

        public Builder sendTime(long sendTime) {
            this.sendTime = sendTime;
            return this;
        }

        public Builder tags(String tags) {
            this.tags = tags;
            return this;
        }

        public Builder keys(String keys) {
            this.keys = keys;
            return this;
        }

        public Builder properties(Map<String, String> properties) {
            this.properties = properties;
            return this;
        }

        public MessageViewDTO build() {
            return new MessageViewDTO(messageId, topic, queueId, offset, body, bodyLength, sendTime, tags, keys,
                    properties);
        }
    }
}
