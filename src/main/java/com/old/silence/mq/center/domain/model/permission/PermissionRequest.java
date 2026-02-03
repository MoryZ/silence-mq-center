package com.old.silence.mq.center.domain.model.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 权限申请实体类
 * 对应数据库表：permission_request
 * 记录用户的权限申请和审批历史
 */
@Entity
@Table(name = "permission_request", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_topic_id", columnList = "topic_id"),
    @Index(name = "idx_permission_type", columnList = "permission_type_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_time", columnList = "created_time"),
    @Index(name = "idx_user_status_time", columnList = "user_id,status,created_time"),
    @Index(name = "idx_approver_status", columnList = "approver_id,status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 申请用户 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 申请用户名
     */
    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    /**
     * 涉及的 Topic ID（NULL 表示全局权限）
     */
    @Column(name = "topic_id")
    private Long topicId;

    /**
     * Topic 名称（冗余字段，用于避免 JOIN）
     */
    @Column(name = "topic_name", length = 255)
    private String topicName;

    /**
     * 权限类型 ID
     */
    @Column(name = "permission_type_id", nullable = false)
    private Integer permissionTypeId;

    /**
     * 权限代码
     * 例如：PRODUCE, CONSUME, CREATE_TOPIC
     */
    @Column(name = "permission_code", nullable = false, length = 50)
    private String permissionCode;

    /**
     * 申请理由
     */
    @Column(name = "request_reason", length = 500)
    private String requestReason;

    /**
     * 申请状态
     * PENDING - 待审批
     * APPROVED - 已批准
     * REJECTED - 已拒绝
     * EXPIRED - 已过期
     * WITHDRAWN - 已撤回
     */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    /**
     * 审批人 ID
     */
    @Column(name = "approver_id")
    private Long approverId;

    /**
     * 审批人名称
     */
    @Column(name = "approver_name", length = 100)
    private String approverName;

    /**
     * 审批意见
     */
    @Column(name = "approval_reason", length = 500)
    private String approvalReason;

    /**
     * 审批时间
     */
    @Column(name = "approval_time")
    private LocalDateTime approvalTime;

    /**
     * 权限过期时间（NULL 表示永久）
     */
    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    /**
     * 申请时间
     */
    @Column(name = "created_time", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_time", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "PermissionRequest{" +
                "id=" + id +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", topicName='" + topicName + '\'' +
                ", permissionCode='" + permissionCode + '\'' +
                ", status='" + status + '\'' +
                ", createdTime=" + createdTime +
                '}';
    }
}
