package com.old.silence.mq.center.dto;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 权限批准请求DTO
 */
public class ApprovePermissionDTO {

    /**
     * 申请ID
     */
    private BigInteger requestId;

    /**
     * 审批人ID
     */
    private BigInteger approverId;

    /**
     * 审批人名称
     */
    private String approverName;

    /**
     * 批准理由
     */
    private String approvalReason;

    /**
     * 权限过期时间
     */
    private Instant expireTime;


    public BigInteger getRequestId() {
        return requestId;
    }

    public void setRequestId(BigInteger requestId) {
        this.requestId = requestId;
    }

    public BigInteger getApproverId() {
        return approverId;
    }

    public void setApproverId(BigInteger approverId) {
        this.approverId = approverId;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public String getApprovalReason() {
        return approvalReason;
    }

    public void setApprovalReason(String approvalReason) {
        this.approvalReason = approvalReason;
    }

    public Instant getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Instant expireTime) {
        this.expireTime = expireTime;
    }
}
