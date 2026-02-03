package com.old.silence.mq.center.domain.model.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Topic 实体类
 * 对应数据库表：topic
 * 记录 RocketMQ 中存在的所有 Topic 元数据
 */
@Entity
@Table(name = "topic", indexes = {
    @Index(name = "idx_cluster", columnList = "cluster_name"),
    @Index(name = "idx_owner_id", columnList = "owner_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Topic 名称（唯一）
     * 例如：Order-Topic, Payment-Topic
     */
    @Column(name = "topic_name", nullable = false, unique = true, length = 255)
    private String topicName;

    /**
     * 所属集群名称
     * 例如：DefaultCluster
     */
    @Column(name = "cluster_name", nullable = false, length = 255)
    private String clusterName;

    /**
     * Topic 描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 所有者用户 ID
     */
    @Column(name = "owner_id")
    private Long ownerId;

    /**
     * 所有者名称
     */
    @Column(name = "owner_name", length = 100)
    private String ownerName;

    /**
     * 是否系统 Topic
     * 0 = 普通 Topic
     * 1 = 系统 Topic
     */
    @Column(name = "is_system_topic")
    @Builder.Default
    private Integer isSystemTopic = 0;

    /**
     * Topic 状态
     * ACTIVE - 激活
     * INACTIVE - 禁用
     * DELETED - 已删除
     */
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    /**
     * 创建时间
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
        return "Topic{" +
                "id=" + id +
                ", topicName='" + topicName + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", ownerId=" + ownerId +
                ", status='" + status + '\'' +
                '}';
    }
}
