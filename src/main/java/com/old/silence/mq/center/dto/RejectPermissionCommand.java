package com.old.silence.mq.center.dto;

import java.math.BigInteger;

/**
 * @author moryzang
 */
public class RejectPermissionCommand {


    /**
     * 申请任务ID
     */
    private BigInteger permissionAuditTaskId;

    /**
     * 审批人名称
     */
    private String approverName;

    private String rejectionReason;

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

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
