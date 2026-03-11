package com.old.silence.mq.center.domain.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.old.silence.mq.center.domain.model.permission.PermissionType;

import java.util.List;
import java.util.Optional;

/**
 * 权限类型 Repository
 */
@Mapper
public interface PermissionTypeRepository extends BaseMapper<PermissionType> {

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
