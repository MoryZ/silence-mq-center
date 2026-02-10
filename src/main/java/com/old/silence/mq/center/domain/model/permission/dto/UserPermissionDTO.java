package com.old.silence.mq.center.domain.model.permission.dto;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 权限查询响应DTO
 */
public class UserPermissionDTO {

    /**
     * 权限ID
     */
    private BigInteger permissionId;

    /**
     * 用户ID
     */
    private BigInteger userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * Topic ID
     */
    private BigInteger topicId;

    /**
     * Topic 名称
     */
    private String topicName;

    /**
     * 权限代码
     */
    private String permissionCode;

    /**
     * 权限名称
     */
    private String permissionName;

    /**
     * 授权人名称
     */
    private String grantedByName;

    /**
     * 授权时间
     */
    private LocalDateTime grantedTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 是否已过期
     */
    private Boolean expired;

    /**
     * 权限状态
     */
    private String status;

    /**
     * 是否有效
     */
    private Boolean valid;

    public BigInteger getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(BigInteger permissionId) {
        this.permissionId = permissionId;
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

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getGrantedByName() {
        return grantedByName;
    }

    public void setGrantedByName(String grantedByName) {
        this.grantedByName = grantedByName;
    }

    public LocalDateTime getGrantedTime() {
        return grantedTime;
    }

    public void setGrantedTime(LocalDateTime grantedTime) {
        this.grantedTime = grantedTime;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }
}
