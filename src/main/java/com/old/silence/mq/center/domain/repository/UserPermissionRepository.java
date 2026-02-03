package com.old.silence.mq.center.domain.repository;

import com.old.silence.mq.center.domain.model.permission.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户权限 Repository
 * 最常用于权限检查的查询
 */
@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {

    /**
     * 检查用户是否有指定权限（最常用）
     * 权限必须是 ACTIVE 状态且未过期
     */
    @Query("SELECT up FROM UserPermission up WHERE up.userId = :userId AND up.topicId = :topicId AND up.permissionCode = :permissionCode AND up.status = 'ACTIVE' AND (up.expireTime IS NULL OR up.expireTime > NOW())")
    Optional<UserPermission> findValidPermission(
        @Param("userId") Long userId,
        @Param("topicId") Long topicId,
        @Param("permissionCode") String permissionCode
    );

    /**
     * 检查用户是否有全局权限（topicId 为 NULL）
     */
    @Query("SELECT up FROM UserPermission up WHERE up.userId = :userId AND up.topicId IS NULL AND up.permissionCode = :permissionCode AND up.status = 'ACTIVE' AND (up.expireTime IS NULL OR up.expireTime > NOW())")
    Optional<UserPermission> findValidGlobalPermission(
        @Param("userId") Long userId,
        @Param("permissionCode") String permissionCode
    );

    /**
     * 查询用户的所有有效权限
     */
    @Query("SELECT up FROM UserPermission up WHERE up.userId = :userId AND up.status = 'ACTIVE' AND (up.expireTime IS NULL OR up.expireTime > NOW())")
    List<UserPermission> findValidPermissionsByUser(@Param("userId") Long userId);

    /**
     * 查询用户在指定 Topic 上的所有有效权限
     */
    @Query("SELECT up FROM UserPermission up WHERE up.userId = :userId AND up.topicId = :topicId AND up.status = 'ACTIVE' AND (up.expireTime IS NULL OR up.expireTime > NOW())")
    List<UserPermission> findValidPermissionsByUserAndTopic(
        @Param("userId") Long userId,
        @Param("topicId") Long topicId
    );

    /**
     * 查询 Topic 的所有权限持有者
     */
    @Query("SELECT up FROM UserPermission up WHERE up.topicId = :topicId AND up.status = 'ACTIVE' AND (up.expireTime IS NULL OR up.expireTime > NOW())")
    List<UserPermission> findValidPermissionsByTopic(@Param("topicId") Long topicId);

    /**
     * 查询所有过期的权限
     */
    @Query("SELECT up FROM UserPermission up WHERE up.isExpired = 1")
    List<UserPermission> findExpiredPermissions();

    /**
     * 查询用户的所有权限（包括过期的）
     */
    @Query("SELECT up FROM UserPermission up WHERE up.userId = :userId")
    List<UserPermission> findAllPermissionsByUser(@Param("userId") Long userId);

    /**
     * 删除用户的指定权限
     */
    @Query("DELETE FROM UserPermission up WHERE up.userId = :userId AND up.topicId = :topicId AND up.permissionCode = :permissionCode")
    void deleteUserPermission(
        @Param("userId") Long userId,
        @Param("topicId") Long topicId,
        @Param("permissionCode") String permissionCode
    );
}
