package com.old.silence.mq.center.domain.model.permission.dto;

import java.math.BigInteger;

/**
 * 权限拒绝请求DTO
 */
public class RejectPermissionDTO {

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
     * 拒绝理由
     */
    private String rejectionReason;

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

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
