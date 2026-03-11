package com.old.silence.mq.center.domain.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.old.silence.mq.center.domain.model.permission.PermissionRequest;

import java.math.BigInteger;
import java.util.List;

/**
 * 权限申请 Repository
 */
@Mapper
public interface PermissionRequestRepository extends BaseMapper<PermissionRequest> {

    /**
     * 查询用户的所有权限申请
     */
    List<PermissionRequest> findByUserId(@Param("userId") BigInteger userId);

    /**
     * 查询指定 Topic 的所有申请
     */
    List<PermissionRequest> findByTopicId(@Param("topicId") BigInteger topicId);

    /**
     * 查询所有待审批的申请
     */
    List<PermissionRequest> findAllPending();

    /**
     * 查询用户在指定 Topic 上的申请
     */
    List<PermissionRequest> findUserTopicPermissionRequest(
            @Param("userId") BigInteger userId,
            @Param("topicId") BigInteger topicId,
            @Param("permissionCode") String permissionCode
    );

    /**
     * 查询用户指定权限已批准的申请
     */
    List<PermissionRequest> findApprovedRequests(
            @Param("userId") BigInteger userId,
            @Param("permissionCode") String permissionCode
    );

    /**
     * 查询指定状态的申请
     */
    List<PermissionRequest> findByStatus(@Param("status") String status);
}
