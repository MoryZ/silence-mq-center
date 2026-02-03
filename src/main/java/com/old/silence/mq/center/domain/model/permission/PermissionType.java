package com.old.silence.mq.center.domain.model.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 权限类型实体类
 * 对应数据库表：permission_type
 * 定义系统支持的所有权限类型（字典表）
 */
@Entity
@Table(name = "permission_type", indexes = {
    @Index(name = "idx_code", columnList = "permission_code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 权限代码（唯一）
     * 例如：PRODUCE, CONSUME, CREATE_TOPIC
     */
    @Column(name = "permission_code", nullable = false, unique = true, length = 50)
    private String permissionCode;

    /**
     * 权限名称
     * 例如：生产消息、消费消息、创建Topic
     */
    @Column(name = "permission_name", nullable = false, length = 100)
    private String permissionName;

    /**
     * 权限描述
     */
    @Column(name = "description", length = 300)
    private String description;

    /**
     * 权限状态
     * ACTIVE - 激活
     * INACTIVE - 禁用
     */
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    /**
     * 创建时间
     */
    @Column(name = "created_time", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "PermissionType{" +
                "id=" + id +
                ", permissionCode='" + permissionCode + '\'' +
                ", permissionName='" + permissionName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
