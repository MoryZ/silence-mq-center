package com.old.silence.mq.center.domain.repository;

import com.old.silence.mq.center.domain.model.permission.PermissionAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限审计日志 Repository
 */
@Repository
public interface PermissionAuditLogRepository extends JpaRepository<PermissionAuditLog, Long> {

    /**
     * 查询操作人的所有审计日志
     */
    @Query("SELECT pal FROM PermissionAuditLog pal WHERE pal.operatorId = :operatorId ORDER BY pal.createdTime DESC")
    List<PermissionAuditLog> findByOperatorId(@Param("operatorId") Long operatorId);

    /**
     * 查询目标用户的所有审计日志
     */
    @Query("SELECT pal FROM PermissionAuditLog pal WHERE pal.targetUserId = :targetUserId ORDER BY pal.createdTime DESC")
    List<PermissionAuditLog> findByTargetUserId(@Param("targetUserId") Long targetUserId);

    /**
     * 查询指定操作类型的日志
     */
    @Query("SELECT pal FROM PermissionAuditLog pal WHERE pal.operationType = :operationType ORDER BY pal.createdTime DESC")
    List<PermissionAuditLog> findByOperationType(@Param("operationType") String operationType);

    /**
     * 查询指定时间范围内的日志
     */
    @Query("SELECT pal FROM PermissionAuditLog pal WHERE pal.createdTime BETWEEN :startTime AND :endTime ORDER BY pal.createdTime DESC")
    List<PermissionAuditLog> findByTimeRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询关联到指定权限申请的日志
     */
    @Query("SELECT pal FROM PermissionAuditLog pal WHERE pal.requestId = :requestId ORDER BY pal.createdTime DESC")
    List<PermissionAuditLog> findByRequestId(@Param("requestId") Long requestId);

    /**
     * 查询用户在指定 Topic 上的所有操作日志
     */
    @Query("SELECT pal FROM PermissionAuditLog pal WHERE pal.targetUserId = :userId AND pal.topicId = :topicId ORDER BY pal.createdTime DESC")
    List<PermissionAuditLog> findByUserAndTopic(
        @Param("userId") Long userId,
        @Param("topicId") Long topicId
    );

    /**
     * 查询所有失败的操作
     */
    @Query("SELECT pal FROM PermissionAuditLog pal WHERE pal.operationResult = 'FAILED' ORDER BY pal.createdTime DESC")
    List<PermissionAuditLog> findFailedOperations();
}
