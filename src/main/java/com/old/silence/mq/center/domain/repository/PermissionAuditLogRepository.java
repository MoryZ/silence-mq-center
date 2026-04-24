package com.old.silence.mq.center.domain.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.old.silence.data.mybatis.projection.ProjectionMapperRepository;
import com.old.silence.mq.center.domain.model.PermissionAuditLog;

import java.math.BigInteger;
import java.util.List;

/**
 * 权限审计日志 Repository
 */
@Mapper
public interface PermissionAuditLogRepository extends ProjectionMapperRepository<PermissionAuditLog, BigInteger> {

    @Select("SELECT * FROM rmq_permission_audit_log WHERE operation_result = false")
    List<PermissionAuditLog> findBySuccessFalse();

}
