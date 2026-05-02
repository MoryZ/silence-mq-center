package com.old.silence.mq.center.dto;

import com.old.silence.data.commons.annotation.RelationalQueryProperty;
import com.old.silence.data.commons.converter.Part;

import java.math.BigInteger;

public class TopicPageQuery {

    private String keyword;


    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private String status;

    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private BigInteger ownerId;

    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private Boolean systemTopic;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigInteger getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(BigInteger ownerId) {
        this.ownerId = ownerId;
    }

    public Boolean getSystemTopic() {
        return systemTopic;
    }

    public void setSystemTopic(Boolean systemTopic) {
        this.systemTopic = systemTopic;
    }
}