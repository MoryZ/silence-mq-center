package com.old.silence.mq.center.domain.model.permission;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 权限审计日志实体类
 * 对应数据库表：permission_audit_log
 * 记录所有权限相关的操作日志，用于审计和追踪
 */
@Entity
@Table(name = "permission_audit_log", indexes = {
    @Index(name = "idx_operation_type", columnList = "operation_type"),
    @Index(name = "idx_operator_id", columnList = "operator_id"),
    @Index(name = "idx_target_user_id", columnList = "target_user_id"),
    @Index(name = "idx_request_id", columnList = "request_id"),
    @Index(name = "idx_created_time", columnList = "created_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 操作类型
     * REQUEST - 申请权限
     * APPROVE - 批准申请
     * REJECT - 拒绝申请
     * GRANT - 授予权限
     * REVOKE - 撤销权限
     * EXPIRE - 权限过期
     */
    @Column(name = "operation_type", nullable = false, length = 50)
    private String operationType;

    /**
     * 操作人 ID
     */
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    /**
     * 操作人名称
     */
    @Column(name = "operator_name", nullable = false, length = 100)
    private String operatorName;

    /**
     * 目标用户 ID
     */
    @Column(name = "target_user_id")
    private Long targetUserId;

    /**
     * 目标用户名
     */
    @Column(name = "target_user_name", length = 100)
    private String targetUserName;

    /**
     * Topic ID
     */
    @Column(name = "topic_id")
    private Long topicId;

    /**
     * Topic 名称
     */
    @Column(name = "topic_name", length = 255)
    private String topicName;

    /**
     * 权限代码
     */
    @Column(name = "permission_code", length = 50)
    private String permissionCode;

    /**
     * 关联的权限申请 ID
     */
    @Column(name = "request_id")
    private Long requestId;

    /**
     * 操作详情（JSON 格式）
     */
    @Column(name = "operation_details", columnDefinition = "JSON")
    @JsonRawValue
    private String operationDetails;

    /**
     * 操作结果
     * SUCCESS - 成功
     * FAILED - 失败
     */
    @Column(name = "operation_result", length = 20)
    @Builder.Default
    private String operationResult = "SUCCESS";

    /**
     * 错误信息（仅在失败时）
     */
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    /**
     * 操作人 IP 地址
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * 操作时间
     */
    @Column(name = "created_time", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "PermissionAuditLog{" +
                "id=" + id +
                ", operationType='" + operationType + '\'' +
                ", operatorId=" + operatorId +
                ", operatorName='" + operatorName + '\'' +
                ", targetUserId=" + targetUserId +
                ", permissionCode='" + permissionCode + '\'' +
                ", operationResult='" + operationResult + '\'' +
                ", createdTime=" + createdTime +
                '}';
    }
}
