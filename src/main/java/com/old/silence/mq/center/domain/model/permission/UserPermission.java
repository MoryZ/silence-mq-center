package com.old.silence.mq.center.domain.model.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户权限实体类
 * 对应数据库表：user_permission
 * 存储已批准的、生效的用户权限（权限的最终映射）
 * 
 * 权限检查时直接查询此表，性能最优化
 */
@Entity
@Table(name = "user_permission", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_topic_id", columnList = "topic_id"),
    @Index(name = "idx_permission_type", columnList = "permission_type_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_expire_time", columnList = "expire_time"),
    @Index(name = "idx_user_active", columnList = "user_id,status"),
    @Index(name = "idx_permission_active", columnList = "permission_type_id,status")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_topic_perm", columnNames = {"user_id", "topic_id", "permission_type_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 用户名
     */
    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    /**
     * Topic ID（NULL 表示全局权限）
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
     * 授权人 ID
     */
    @Column(name = "granted_by_id")
    private Long grantedById;

    /**
     * 授权人名称
     */
    @Column(name = "granted_by_name", length = 100)
    private String grantedByName;

    /**
     * 授权时间
     */
    @Column(name = "granted_time", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime grantedTime;

    /**
     * 权限过期时间（NULL 表示永久有效）
     */
    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    /**
     * 是否已过期
     * 0 = 未过期
     * 1 = 已过期
     */
    @Column(name = "is_expired")
    @Builder.Default
    private Integer isExpired = 0;

    /**
     * 权限状态
     * ACTIVE - 激活/有效
     * REVOKED - 已撤销
     * EXPIRED - 已过期
     */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    /**
     * 更新时间
     */
    @Column(name = "updated_time", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        grantedTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
        // 自动更新过期状态
        if (expireTime != null && LocalDateTime.now().isAfter(expireTime)) {
            isExpired = 1;
            status = "EXPIRED";
        }
    }

    /**
     * 检查权限是否有效
     */
    public boolean isValid() {
        if (!"ACTIVE".equals(status)) {
            return false;
        }
        if (isExpired == 1) {
            return false;
        }
        if (expireTime != null && LocalDateTime.now().isAfter(expireTime)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UserPermission{" +
                "id=" + id +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", topicName='" + topicName + '\'' +
                ", permissionCode='" + permissionCode + '\'' +
                ", status='" + status + '\'' +
                ", isExpired=" + isExpired +
                '}';
    }
}
