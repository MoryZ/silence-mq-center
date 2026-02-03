package com.old.silence.mq.center.domain.repository;

import com.old.silence.mq.center.domain.model.permission.PermissionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 权限申请 Repository
 */
@Repository
public interface PermissionRequestRepository extends JpaRepository<PermissionRequest, Long> {

    /**
     * 查询用户的所有权限申请
     */
    @Query("SELECT pr FROM PermissionRequest pr WHERE pr.userId = :userId ORDER BY pr.createdTime DESC")
    List<PermissionRequest> findByUserId(@Param("userId") Long userId);

    /**
     * 查询指定 Topic 的所有申请
     */
    @Query("SELECT pr FROM PermissionRequest pr WHERE pr.topicId = :topicId ORDER BY pr.createdTime DESC")
    List<PermissionRequest> findByTopicId(@Param("topicId") Long topicId);

    /**
     * 查询所有待审批的申请
     */
    @Query("SELECT pr FROM PermissionRequest pr WHERE pr.status = 'PENDING' ORDER BY pr.createdTime ASC")
    List<PermissionRequest> findAllPending();

    /**
     * 查询用户在指定 Topic 上的申请
     */
    @Query("SELECT pr FROM PermissionRequest pr WHERE pr.userId = :userId AND pr.topicId = :topicId AND pr.permissionCode = :permissionCode")
    List<PermissionRequest> findUserTopicPermissionRequest(
        @Param("userId") Long userId,
        @Param("topicId") Long topicId,
        @Param("permissionCode") String permissionCode
    );

    /**
     * 查询用户指定权限已批准的申请
     */
    @Query("SELECT pr FROM PermissionRequest pr WHERE pr.userId = :userId AND pr.permissionCode = :permissionCode AND pr.status = 'APPROVED' AND (pr.expireTime IS NULL OR pr.expireTime > NOW())")
    List<PermissionRequest> findApprovedRequests(
        @Param("userId") Long userId,
        @Param("permissionCode") String permissionCode
    );

    /**
     * 查询指定状态的申请
     */
    @Query("SELECT pr FROM PermissionRequest pr WHERE pr.status = :status ORDER BY pr.createdTime DESC")
    List<PermissionRequest> findByStatus(@Param("status") String status);
}
