package com.old.silence.mq.center.dto;

import java.math.BigInteger;

/**
 * 权限批准请求DTO
 */
public class ApprovePermissionCommand {

    /**
     * 申请任务ID
     */
    private BigInteger permissionAuditTaskId;

    /**
     * 审批人名称
     */
    private String approverName;

    /**
     * 批准理由
     */
    private String approvalReason;

    public BigInteger getPermissionAuditTaskId() {
        return permissionAuditTaskId;
    }

    public void setPermissionAuditTaskId(BigInteger permissionAuditTaskId) {
        this.permissionAuditTaskId = permissionAuditTaskId;
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
}
