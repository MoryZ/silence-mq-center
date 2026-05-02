package com.old.silence.mq.center.dto;

import java.time.Instant;

/**
 * 权限申请请求DTO
 */
public class PermissionRequestCommand {

    private String applyName;


    /**
     * 申请的权限代码
     */
    private String permissionCode;

    /**
     * 申请理由
     */
    private String requestReason;

    private Instant expireTime;

    private String auditChain;

    public String getApplyName() {
        return applyName;
    }

    public void setApplyName(String applyName) {
        this.applyName = applyName;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getRequestReason() {
        return requestReason;
    }

    public void setRequestReason(String requestReason) {
        this.requestReason = requestReason;
    }

    public Instant getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Instant expireTime) {
        this.expireTime = expireTime;
    }

    public String getAuditChain() {
        return auditChain;
    }

    public void setAuditChain(String auditChain) {
        this.auditChain = auditChain;
    }
}
