package com.old.silence.mq.center.domain.repository;

import org.apache.ibatis.annotations.Param;
import com.old.silence.data.mybatis.projection.ProjectionMapperRepository;
import com.old.silence.mq.center.domain.model.PermissionType;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * 权限类型 Repository
 */
public interface PermissionTypeRepository extends ProjectionMapperRepository<PermissionType, BigInteger> {

    /**
     * 根据权限代码查找权限类型
     */
    Optional<PermissionType> findByPermissionCode(@Param("permissionCode") String permissionCode);

    /**
     * 查询所有激活的权限类型
     */
    List<PermissionType> findAllActive();

    /**
     * 根据权限名称查找权限类型
     */
    Optional<PermissionType> findByPermissionName(@Param("permissionName") String permissionName);
}
