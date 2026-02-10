package com.old.silence.mq.center.domain.model.permission.dto;

import java.math.BigInteger;

/**
 * 权限申请请求DTO
 */
public class PermissionRequestDTO {

    /**
     * 申请用户ID
     */
    private BigInteger userId;

    /**
     * 申请用户名
     */
    private String userName;

    /**
     * Topic ID（NULL表示全局权限）
     */
    private BigInteger topicId;

    /**
     * Topic 名称
     */
    private String topicName;

    /**
     * 申请的权限代码
     */
    private String permissionCode;

    /**
     * 申请理由
     */
    private String reason;


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

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
