package com.old.silence.mq.center.domain.repository;

import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.old.silence.mq.center.domain.model.permission.UserPermission;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * 用户权限 Repository
 * 最常用于权限检查的查询
 */
@Repository
public interface UserPermissionRepository extends BaseMapper<UserPermission> {

    /**
     * 检查用户是否有指定权限（最常用）
     * 权限必须是 ACTIVE 状态且未过期
     */
    @Select("SELECT up FROM user_permission up WHERE up.userId = :userId AND up.topicId = :topicId AND up.permissionCode = :permissionCode AND up.status = 'ACTIVE' AND (up.expireTime IS NULL OR up.expireTime > NOW())")
    Optional<UserPermission> findValidPermission(
            @Param("userId") BigInteger userId,
            @Param("topicId") BigInteger topicId,
            @Param("permissionCode") String permissionCode
    );

    /**
     * 检查用户是否有全局权限（topicId 为 NULL）
     */
    @Select("SELECT up FROM user_permission up WHERE up.userId = :userId AND up.topicId IS NULL AND up.permissionCode = :permissionCode AND up.status = 'ACTIVE' AND (up.expireTime IS NULL OR up.expireTime > NOW())")
    Optional<UserPermission> findValidGlobalPermission(
            @Param("userId") BigInteger userId,
            @Param("permissionCode") String permissionCode
    );

    /**
     * 查询用户的所有有效权限
     */
    @Select("SELECT up FROM user_permission up WHERE up.userId = :userId AND up.status = 'ACTIVE' AND (up.expireTime IS NULL OR up.expireTime > NOW())")
    List<UserPermission> findValidPermissionsByUser(@Param("userId") BigInteger userId);

    /**
     * 查询用户在指定 Topic 上的所有有效权限
     */
    @Select("SELECT up FROM user_permission up WHERE up.userId = :userId AND up.topicId = :topicId AND up.status = 'ACTIVE' AND (up.expireTime IS NULL OR up.expireTime > NOW())")
    List<UserPermission> findValidPermissionsByUserAndTopic(
            @Param("userId") BigInteger userId,
            @Param("topicId") BigInteger topicId
    );

    /**
     * 查询 Topic 的所有权限持有者
     */
    @Select("SELECT up FROM user_permission up WHERE up.topicId = :topicId AND up.status = 'ACTIVE' AND (up.expireTime IS NULL OR up.expireTime > NOW())")
    List<UserPermission> findValidPermissionsByTopic(@Param("topicId") BigInteger topicId);

    /**
     * 查询所有过期的权限
     */
    @Select("SELECT up FROM user_permission up WHERE up.isExpired = 1")
    List<UserPermission> findExpiredPermissions();

    /**
     * 查询用户的所有权限（包括过期的）
     */
    @Select("SELECT up FROM user_permission up WHERE up.userId = :userId")
    List<UserPermission> findAllPermissionsByUser(@Param("userId") BigInteger userId);

    /**
     * 删除用户的指定权限
     */
    @Select("DELETE FROM user_permission up WHERE up.userId = :userId AND up.topicId = :topicId AND up.permissionCode = :permissionCode")
    void deleteUserPermission(
            @Param("userId") BigInteger userId,
            @Param("topicId") BigInteger topicId,
            @Param("permissionCode") String permissionCode
    );
}
