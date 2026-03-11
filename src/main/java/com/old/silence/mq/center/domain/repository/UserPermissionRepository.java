package com.old.silence.mq.center.domain.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.old.silence.mq.center.domain.model.permission.UserPermission;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * 用户权限 Repository
 * 最常用于权限检查的查询
 */
@Mapper
public interface UserPermissionRepository extends BaseMapper<UserPermission> {

    /**
     * 检查用户是否有指定权限（最常用）
     * 权限必须是 ACTIVE 状态且未过期
     */
    Optional<UserPermission> findValidPermission(
            @Param("userId") BigInteger userId,
            @Param("topicId") BigInteger topicId,
            @Param("permissionCode") String permissionCode
    );

    /**
     * 检查用户是否有全局权限（topicId 为 NULL）
     */
    Optional<UserPermission> findValidGlobalPermission(
            @Param("userId") BigInteger userId,
            @Param("permissionCode") String permissionCode
    );

    /**
     * 查询用户的所有有效权限
     */
    List<UserPermission> findValidPermissionsByUser(@Param("userId") BigInteger userId);

    /**
     * 查询用户在指定 Topic 上的所有有效权限
     */
    List<UserPermission> findValidPermissionsByUserAndTopic(
            @Param("userId") BigInteger userId,
            @Param("topicId") BigInteger topicId
    );

    /**
     * 查询 Topic 的所有权限持有者
     */
    List<UserPermission> findValidPermissionsByTopic(@Param("topicId") BigInteger topicId);

    /**
     * 查询所有过期的权限
     */
    List<UserPermission> findExpiredPermissions();

    /**
     * 查询用户的所有权限（包括过期的）
     */
    List<UserPermission> findAllPermissionsByUser(@Param("userId") BigInteger userId);

    /**
     * 删除用户的指定权限
     */
    void deleteUserPermission(
            @Param("userId") BigInteger userId,
            @Param("topicId") BigInteger topicId,
            @Param("permissionCode") String permissionCode
    );
}
