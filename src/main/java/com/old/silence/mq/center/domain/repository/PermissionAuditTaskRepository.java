package com.old.silence.mq.center.domain.repository;

import org.apache.ibatis.annotations.Mapper;
import com.old.silence.data.mybatis.projection.ProjectionMapperRepository;
import com.old.silence.mq.center.domain.model.PermissionAuditTask;

import java.math.BigInteger;

/**
 * 权限审计日志 Repository
 */
@Mapper
public interface PermissionAuditTaskRepository extends ProjectionMapperRepository<PermissionAuditTask, BigInteger> {


}
