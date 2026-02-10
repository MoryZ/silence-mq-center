package com.old.silence.mq.center.domain.dto;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 赋予权限 DTO
 * <p>
 * 用于管理员直接为用户赋予权限的请求
 *
 * @author Silence
 * @since 2024-01-01
 */
public class GrantPermissionDTO {

    /**
     * 要赋予权限的用户 ID
     */
    private BigInteger userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * Topic ID（为 null 表示赋予全局权限）
     */
    private BigInteger topicId;

    /**
     * 权限代码（如：PRODUCE、CONSUME、CREATE_TOPIC）
     */
    private String permissionCode;

    /**
     * 权限过期时间（为 null 表示永不过期）
     */
    private LocalDateTime expireTime;

    public GrantPermissionDTO() {
    }

    public GrantPermissionDTO(BigInteger userId, String userName, BigInteger topicId, String permissionCode,
                              LocalDateTime expireTime) {
        this.userId = userId;
        this.userName = userName;
        this.topicId = topicId;
        this.permissionCode = permissionCode;
        this.expireTime = expireTime;
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

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }
}
