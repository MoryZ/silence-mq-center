package com.old.silence.mq.center.dto;

import java.math.BigInteger;
import java.time.Instant;

/**
 * @author moryzang
 */
public class UserPermissionDTO {

    private BigInteger permissionId;


    private BigInteger userId;

    private String userName;

    private BigInteger topicId;

    private String permissionCode;

    private String status;

    private Instant expireTime;

    private Instant grantedTime;

    private Boolean expired;

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

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Instant expireTime) {
        this.expireTime = expireTime;
    }

    public Instant getGrantedTime() {
        return grantedTime;
    }

    public void setGrantedTime(Instant grantedTime) {
        this.grantedTime = grantedTime;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }
}
