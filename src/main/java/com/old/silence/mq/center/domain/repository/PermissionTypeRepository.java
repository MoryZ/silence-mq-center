package com.old.silence.mq.center.domain.repository;

import com.old.silence.mq.center.domain.model.permission.PermissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 权限类型 Repository
 */
@Repository
public interface PermissionTypeRepository extends JpaRepository<PermissionType, Integer> {

    /**
     * 根据权限代码查找权限类型
     */
    Optional<PermissionType> findByPermissionCode(String permissionCode);

    /**
     * 查询所有激活的权限类型
     */
    @Query("SELECT pt FROM PermissionType pt WHERE pt.status = 'ACTIVE'")
    List<PermissionType> findAllActive();

    /**
     * 根据权限名称查找权限类型
     */
    Optional<PermissionType> findByPermissionName(String permissionName);
}
