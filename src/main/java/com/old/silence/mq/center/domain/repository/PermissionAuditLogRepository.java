package com.old.silence.mq.center.domain.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.old.silence.mq.center.domain.model.permission.PermissionAuditLog;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限审计日志 Repository
 */
@Mapper
public interface PermissionAuditLogRepository extends BaseMapper<PermissionAuditLog> {

    /**
     * 查询操作人的所有审计日志
     */
    List<PermissionAuditLog> findByOperatorId(@Param("operatorId") BigInteger operatorId);

    /**
     * 查询目标用户的所有审计日志
     */
    List<PermissionAuditLog> findByTargetUserId(@Param("targetUserId") BigInteger targetUserId);

    /**
     * 查询指定操作类型的日志
     */
    List<PermissionAuditLog> findByOperationType(@Param("operationType") String operationType);

    /**
     * 查询指定时间范围内的日志
     */
    List<PermissionAuditLog> findByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询关联到指定权限申请的日志
     */
    List<PermissionAuditLog> findByRequestId(@Param("requestId") BigInteger requestId);

    /**
     * 查询用户在指定 Topic 上的所有操作日志
     */
    List<PermissionAuditLog> findByUserAndTopic(
            @Param("userId") BigInteger userId,
            @Param("topicId") BigInteger topicId
    );

    /**
     * 查询所有失败的操作
     */
    List<PermissionAuditLog> findFailedOperations();
}
