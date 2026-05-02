package com.old.silence.mq.center.vo;

import java.math.BigInteger;

/**
 * 权限检查响应VO
 * <p>
 * 返回用户对特定权限的检查结果
 *
 * @author Silence
 * @since 2024-01-01
 */
public class CheckPermissionResponseVO {

    private BigInteger userId;
    private BigInteger topicId;
    private String permissionCode;
    private boolean hasPermission;

    public CheckPermissionResponseVO() {
    }

    public CheckPermissionResponseVO(BigInteger userId, BigInteger topicId,
                                     String permissionCode, boolean hasPermission) {
        this.userId = userId;
        this.topicId = topicId;
        this.permissionCode = permissionCode;
        this.hasPermission = hasPermission;
    }

    public BigInteger getUserId() {
        return userId;
    }

    public void setUserId(BigInteger userId) {
        this.userId = userId;
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

    public boolean isHasPermission() {
        return hasPermission;
    }

    public void setHasPermission(boolean hasPermission) {
        this.hasPermission = hasPermission;
    }
}
