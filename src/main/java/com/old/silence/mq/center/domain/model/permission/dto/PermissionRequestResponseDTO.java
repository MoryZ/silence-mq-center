package com.old.silence.mq.center.domain.model.permission.dto;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 权限申请响应DTO
 */
public class PermissionRequestResponseDTO {

    /**
     * 申请ID
     */
    private BigInteger requestId;

    /**
     * 申请用户ID
     */
    private BigInteger userId;

    /**
     * 申请用户名
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
     * 申请理由
     */
    private String requestReason;

    /**
     * 申请状态
     */
    private String status;

    /**
     * 审批人ID
     */
    private BigInteger approverId;

    /**
     * 审批人名称
     */
    private String approverName;

    /**
     * 审批意见
     */
    private String approvalReason;


    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;

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

    public String getRequestReason() {
        return requestReason;
    }

    public void setRequestReason(String requestReason) {
        this.requestReason = requestReason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public LocalDateTime getApprovalTime() {
        return approvalTime;
    }

    public void setApprovalTime(LocalDateTime approvalTime) {
        this.approvalTime = approvalTime;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }
}
