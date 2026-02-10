package com.old.silence.mq.center.domain.model.permission.dto;

import java.math.BigInteger;
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
    private LocalDateTime expireTime;


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

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }
}
