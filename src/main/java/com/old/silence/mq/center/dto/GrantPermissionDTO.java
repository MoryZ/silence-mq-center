package com.old.silence.mq.center.dto;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 权限赋予请求DTO
 * <p>
 * 用于管理员直接为用户赋予权限
 * 
 * @author Silence
 * @since 2024-01-01
 */
public class GrantPermissionDTO {

    /**
     * 目标用户ID
     */
    private BigInteger userId;

    /**
     * 目标用户名
     */
    private String userName;

    /**
     * Topic ID
     */
    private BigInteger topicId;

    /**
     * 权限代码（如PRODUCE, CONSUME等）
     */
    private String permissionCode;

    /**
     * 权限过期时间
     */
    private Instant expireTime;

    public GrantPermissionDTO() {
    }

    public BigInteger getUserId() {
        return userId;
    }

    public void setUserId(BigInteger userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BigInteger getTopicId() {
        return topicId;
    }

    public void setTopicId(BigInteger topicId) {
        this.topicId = topicId;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public Instant getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Instant expireTime) {
        this.expireTime = expireTime;
    }
}
