package com.old.silence.mq.center.domain.service.permission;

import com.old.silence.mq.center.domain.model.permission.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 权限服务接口
 * 定义权限管理的核心操作
 */
public interface PermissionService {

    /**
     * 申请权限
     * @param userId 申请用户ID
     * @param userName 申请用户名
     * @param topicId Topic ID（NULL表示全局权限）
     * @param permissionCode 申请的权限代码
     * @param reason 申请理由
     * @return 申请记录
     */
    PermissionRequest requestPermission(Long userId, String userName, Long topicId, String permissionCode, String reason);

    /**
     * 批准权限申请
     * @param requestId 申请ID
     * @param approverId 审批人ID
     * @param approverName 审批人名称
     * @param approvalReason 批准理由
     * @param expireTime 权限过期时间（NULL表示永久）
     * @return 授予的权限
     */
    UserPermission approvePermission(Long requestId, Long approverId, String approverName, String approvalReason, LocalDateTime expireTime);

    /**
     * 拒绝权限申请
     * @param requestId 申请ID
     * @param approverId 审批人ID
     * @param approverName 审批人名称
     * @param rejectionReason 拒绝理由
     */
    void rejectPermission(Long requestId, Long approverId, String approverName, String rejectionReason);

    /**
     * 直接授予权限（不需要申请流程）
     * @param userId 用户ID
     * @param userName 用户名
     * @param topicId Topic ID
     * @param permissionCode 权限代码
     * @param grantedById 授予人ID
     * @param grantedByName 授予人名称
     * @param expireTime 过期时间
     * @return 授予的权限
     */
    UserPermission grantPermission(Long userId, String userName, Long topicId, String permissionCode, 
                                   Long grantedById, String grantedByName, LocalDateTime expireTime);

    /**
     * 撤销权限
     * @param userId 用户ID
     * @param topicId Topic ID
     * @param permissionCode 权限代码
     */
    void revokePermission(Long userId, Long topicId, String permissionCode);

    /**
     * 权限过期处理
     * @param permissionId 权限ID
     */
    void expirePermission(Long permissionId);

    /**
     * 自动处理过期权限
     * @return 过期的权限数量
     */
    int expireExpiredPermissions();

    // ==================== 权限检查方法 ====================

    /**
     * 检查用户是否有权限
     * @param userId 用户ID
     * @param topicId Topic ID
     * @param permissionCode 权限代码
     * @return true-有权限，false-无权限
     */
    boolean hasPermission(Long userId, Long topicId, String permissionCode);

    /**
     * 检查用户权限（如果无权限抛出异常）
     * @param userId 用户ID
     * @param topicId Topic ID
     * @param permissionCode 权限代码
     * @throws PermissionDeniedException 无权限时抛出
     */
    void checkPermission(Long userId, Long topicId, String permissionCode) throws PermissionDeniedException;

    /**
     * 检查用户是否有全局权限
     * @param userId 用户ID
     * @param permissionCode 权限代码
     * @return true-有权限，false-无权限
     */
    boolean hasGlobalPermission(Long userId, String permissionCode);

    // ==================== 权限查询方法 ====================

    /**
     * 查询用户的所有有效权限
     * @param userId 用户ID
     * @return 权限列表
     */
    List<UserPermission> getUserPermissions(Long userId);

    /**
     * 查询用户在指定Topic上的所有有效权限
     * @param userId 用户ID
     * @param topicId Topic ID
     * @return 权限列表
     */
    List<UserPermission> getUserPermissionsByTopic(Long userId, Long topicId);

    /**
     * 查询Topic的所有权限持有者
     * @param topicId Topic ID
     * @return 权限列表
     */
    List<UserPermission> getTopicPermissions(Long topicId);

    /**
     * 查询用户的所有权限申请（包括所有状态）
     * @param userId 用户ID
     * @return 申请列表
     */
    List<PermissionRequest> getUserRequests(Long userId);

    /**
     * 查询所有待审批的申请
     * @return 申请列表
     */
    List<PermissionRequest> getPendingRequests();

    /**
     * 查询所有失败的操作审计日志
     * @return 日志列表
     */
    List<PermissionAuditLog> getFailedAuditLogs();

    // ==================== 辅助方法 ====================

    /**
     * 获取指定权限ID的权限
     * @param permissionId 权限ID
     * @return 权限对象
     */
    Optional<UserPermission> getPermissionById(Long permissionId);

    /**
     * 获取指定申请ID的申请
     * @param requestId 申请ID
     * @return 申请对象
     */
    Optional<PermissionRequest> getRequestById(Long requestId);

    /**
     * 权限不足异常
     */
    class PermissionDeniedException extends RuntimeException {
        private final Long userId;
        private final Long topicId;
        private final String permissionCode;

        public PermissionDeniedException(Long userId, Long topicId, String permissionCode) {
            super(String.format("用户 %d 在Topic %d 上没有 %s 权限", userId, topicId, permissionCode));
            this.userId = userId;
            this.topicId = topicId;
            this.permissionCode = permissionCode;
        }

        public Long getUserId() {
            return userId;
        }

        public Long getTopicId() {
            return topicId;
        }

        public String getPermissionCode() {
            return permissionCode;
        }
    }
}
