package com.old.silence.mq.center.domain.service;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.mq.center.domain.model.PermissionAuditLog;
import com.old.silence.mq.center.domain.model.PermissionRequest;
import com.old.silence.mq.center.domain.model.UserPermission;
import com.old.silence.mq.center.domain.repository.PermissionAuditLogRepository;
import com.old.silence.mq.center.domain.repository.PermissionRequestRepository;
import com.old.silence.mq.center.domain.repository.UserPermissionRepository;
import com.old.silence.mq.center.exception.ServiceException;

/**
 * 权限管理业务服务
 * <p>
 * 提供权限申请、审批、赋予、撤销等核心业务功能
 * 不直接返回HTTP响应，由Controller处理响应封装
 *
 * @author Silence
 * @since 2024-01-01
 */
@Service
public class PermissionService {

    private static final Logger log = LoggerFactory.getLogger(PermissionService.class);

    private final PermissionRequestRepository permissionRequestRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final PermissionAuditLogRepository permissionAuditLogRepository;

    public PermissionService(
            PermissionRequestRepository permissionRequestRepository,
            UserPermissionRepository userPermissionRepository,
            PermissionAuditLogRepository permissionAuditLogRepository) {
        this.permissionRequestRepository = permissionRequestRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.permissionAuditLogRepository = permissionAuditLogRepository;
    }

    /**
     * 用户申请权限
     * <p>
     * 创建新的权限申请，状态为待审批
     *
     * @param userId         申请用户ID
     * @param userName       申请用户名
     * @param topicId        Topic ID（null表示全局权限）
     * @param permissionCode 权限代码
     * @param reason         申请理由
     * @return 创建的权限申请
     */
    @Transactional
    public BigInteger requestPermission(
            BigInteger userId,
            String userName,
            BigInteger topicId,
            String permissionCode,
            String reason) {
        try {
            PermissionRequest request = new PermissionRequest();
            request.setUserId(userId);
            request.setUserName(userName);
            request.setTopicId(topicId);
            request.setPermissionCode(permissionCode);
            request.setRequestReason(reason);
            request.setStatus("PENDING");

             permissionRequestRepository.insert(request);
            log.info("用户权限申请已创建: userId={}, topicId={}, permissionCode={}, requestId={}",
                    userId, topicId, permissionCode, request.getId());

            return request.getId();
        } catch (Exception e) {
            log.error("创建权限申请失败: userId={}, topicId={}", userId, topicId, e);
            throw new ServiceException(500, "权限申请失败: " + e.getMessage());
        }
    }

    /**
     * 审批权限申请（通过）
     * <p>
     * 管理员同意权限申请，为申请用户创建有效期内的权限
     *
     * @param requestId      权限申请ID
     * @param approverId     审批人ID
     * @param approverName   审批人名称
     * @param approvalReason 审批理由
     * @param expireTime     权限过期时间
     */
    @Transactional
    public void approvePermission(
            BigInteger requestId,
            BigInteger approverId,
            String approverName,
            String approvalReason,
            Instant expireTime) {
        try {
            PermissionRequest request = permissionRequestRepository.findById(requestId)
                    .orElseThrow(() -> new ServiceException(404, "权限申请不存在"));

            // 创建用户权限
            UserPermission permission = new UserPermission();
            permission.setUserId(request.getUserId());
            permission.setUserName(request.getUserName());
            permission.setTopicId(request.getTopicId());
            permission.setPermissionCode(request.getPermissionCode());
            permission.setStatus("APPROVED");
            permission.setExpireTime(expireTime);
            permission.setGrantedTime(Instant.now());

            userPermissionRepository.insert(permission);

            // 更新权限申请状态
            request.setStatus("APPROVED");
            request.setApprovalReason(approvalReason);
            request.setApprovalTime(Instant.now());
            request.setApproverId(approverId);
            permissionRequestRepository.insert(request);

            // 记录审计日志
            logAuditAction(
                    approverId,
                    approverName,
                    "APPROVE_PERMISSION",
                    "Approved permission request: " + requestId,
                    true,
                    null
            );

            log.info("权限申请已审批通过: requestId={}, userId={}, topicId={}, approverId={}",
                    requestId, request.getUserId(), request.getTopicId(), approverId);

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("审批权限申请失败: requestId={}", requestId, e);
            logAuditAction(
                    approverId,
                    approverName,
                    "APPROVE_PERMISSION",
                    "Failed to approve: " + e.getMessage(),
                    false,
                    e.getMessage()
            );
            throw new ServiceException(500, "审批失败: " + e.getMessage());
        }
    }

    /**
     * 拒绝权限申请
     * <p>
     * 管理员拒绝权限申请
     *
     * @param requestId        权限申请ID
     * @param approverId       审批人ID
     * @param approverName     审批人名称
     * @param rejectionReason  拒绝理由
     */
    @Transactional
    public void rejectPermission(
            BigInteger requestId,
            BigInteger approverId,
            String approverName,
            String rejectionReason) {
        try {
            PermissionRequest request = permissionRequestRepository.findById(requestId)
                    .orElseThrow(() -> new ServiceException(404, "权限申请不存在"));

            // 更新权限申请状态为已拒绝
            request.setStatus("REJECTED");
            request.setApprovalReason(rejectionReason);
            request.setApprovalTime(Instant.now());
            request.setApproverId(approverId);
            permissionRequestRepository.save(request);

            // 记录审计日志
            logAuditAction(
                    approverId,
                    approverName,
                    "REJECT_PERMISSION",
                    "Rejected permission request: " + requestId,
                    true,
                    null
            );

            log.info("权限申请已拒绝: requestId={}, userId={}, rejectReason={}",
                    requestId, request.getUserId(), rejectionReason);

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("拒绝权限申请失败: requestId={}", requestId, e);
            logAuditAction(
                    approverId,
                    approverName,
                    "REJECT_PERMISSION",
                    "Failed to reject: " + e.getMessage(),
                    false,
                    e.getMessage()
            );
            throw new ServiceException(500, "操作失败: " + e.getMessage());
        }
    }

    /**
     * 直接赋予权限（管理员操作）
     * <p>
     * 管理员直接为用户赋予权限，无需审批流程
     *
     * @param userId         目标用户ID
     * @param userName       目标用户名
     * @param topicId        Topic ID（null表示全局权限）
     * @param permissionCode 权限代码
     * @param grantedById    赋予权限的管理员ID
     * @param grantedByName  赋予权限的管理员名称
     * @param expireTime     权限过期时间
     */
    @Transactional
    public void grantPermission(
            BigInteger userId,
            String userName,
            BigInteger topicId,
            String permissionCode,
            BigInteger grantedById,
            String grantedByName,
            Instant expireTime) {
        try {
            // 检查是否已存在相同权限
            userPermissionRepository.findByQuery(new LambdaQueryWrapper<UserPermission>()
                    .eq(UserPermission::getUserId, userId)
                    .eq(UserPermission::getTopicId, topicId)
                    .eq(UserPermission::getPermissionCode, permissionCode));


            // 创建新权限
            UserPermission permission = new UserPermission();
            permission.setUserId(userId);
            permission.setUserName(userName);
            permission.setTopicId(topicId);
            permission.setPermissionCode(permissionCode);
            permission.setStatus("APPROVED");
            permission.setExpireTime(expireTime);
            permission.setGrantedTime(Instant.now());

            userPermissionRepository.insert(permission);

            // 记录审计日志
            logAuditAction(
                    grantedById,
                    grantedByName,
                    "GRANT_PERMISSION",
                    "Granted permission to user: " + userId + ", code: " + permissionCode,
                    true,
                    null
            );

            log.info("权限已直接赋予: userId={}, topicId={}, permissionCode={}, grantedBy={}",
                    userId, topicId, permissionCode, grantedById);

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("赋予权限失败: userId={}, topicId={}", userId, topicId, e);
            logAuditAction(
                    grantedById,
                    grantedByName,
                    "GRANT_PERMISSION",
                    "Failed to grant: " + e.getMessage(),
                    false,
                    e.getMessage()
            );
            throw new ServiceException(500, "赋予权限失败: " + e.getMessage());
        }
    }

    /**
     * 撤销权限（管理员操作）
     * <p>
     * 管理员撤销用户的权限
     *
     * @param userId         用户ID
     * @param topicId        Topic ID
     * @param permissionCode 权限代码
     */
    @Transactional
    public void revokePermission(
            BigInteger userId,
            BigInteger topicId,
            String permissionCode) {
        try {
            UserPermission permission = userPermissionRepository.findByUserIdAndTopicIdAndPermissionCode(
                    userId, topicId, permissionCode);


            userPermissionRepository.delete(permission);

            log.info("权限已撤销: userId={}, topicId={}, permissionCode={}",
                    userId, topicId, permissionCode);

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("撤销权限失败: userId={}, topicId={}", userId, topicId, e);
            throw new ServiceException(500, "撤销权限失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的所有权限
     *
     * @param userId 用户ID
     * @return 用户权限列表
     */
    public List<UserPermission> getUserPermissions(BigInteger userId) {
        try {
            return userPermissionRepository.findByQuery(new LambdaQueryWrapper<UserPermission>()
                    .eq(UserPermission::getUserId, userId));
        } catch (Exception e) {
            log.error("查询用户权限失败: userId={}", userId, e);
            throw new ServiceException(500, "查询权限列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取某个Topic的所有权限
     *
     * @param topicId Topic ID
     * @return 权限列表
     */
    public List<UserPermission> getTopicPermissions(BigInteger topicId) {
        try {
            return userPermissionRepository.findByQuery(new LambdaQueryWrapper<UserPermission>()
                    .eq(UserPermission::getTopicId, topicId));
        } catch (Exception e) {
            log.error("查询Topic权限失败: topicId={}", topicId, e);
            throw new ServiceException(500, "查询权限列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户在某个Topic上的权限
     *
     * @param userId  用户ID
     * @param topicId Topic ID
     * @return 权限列表
     */
    public List<UserPermission> getUserPermissionsByTopic(BigInteger userId, BigInteger topicId) {
        try {
            return userPermissionRepository.findByQuery(new LambdaQueryWrapper<UserPermission>()
                    .eq(UserPermission::getUserId, userId)
                    .eq(UserPermission::getTopicId, topicId)
            );
        } catch (Exception e) {
            log.error("查询用户Topic权限失败: userId={}, topicId={}", userId, topicId, e);
            throw new ServiceException(500, "查询权限列表失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户是否拥有指定权限
     *
     * @param userId         用户ID
     * @param topicId        Topic ID（null表示检查全局权限）
     * @param permissionCode 权限代码
     * @return true表示有权限
     */
    public boolean hasPermission(
            BigInteger userId,
            BigInteger topicId,
            String permissionCode) {
        try {
            UserPermission userPermission = userPermissionRepository
                    .findByUserIdAndTopicIdAndPermissionCode(userId, topicId, permissionCode);

            if (userPermission != null) {
                // 检查权限是否过期
                if (userPermission.getExpireTime() != null && userPermission.getExpireTime().isBefore(Instant.now())) {
                    log.warn("权限已过期: userId={}, topicId={}, permissionCode={}",
                            userId, topicId, permissionCode);
                    return false;
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("权限检查失败: userId={}, topicId={}, permissionCode={}", 
                    userId, topicId, permissionCode, e);
            return false;
        }
    }

    /**
     * 获取待审批的权限申请列表
     *
     * @return 待审批申请列表
     */
    public List<PermissionRequest> getPendingRequests() {
        try {
            return permissionRequestRepository.findByStatus("PENDING");
        } catch (Exception e) {
            log.error("查询待审批申请失败", e);
            throw new ServiceException(500, "查询申请列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的权限申请记录
     *
     * @param userId 用户ID
     * @return 申请记录列表
     */
    public List<PermissionRequest> getUserRequests(BigInteger userId) {
        try {
            return permissionRequestRepository.findByUserId(userId);
        } catch (Exception e) {
            log.error("查询用户申请记录失败: userId={}", userId, e);
            throw new ServiceException(500, "查询申请记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取权限审计日志
     *
     * @return 审计日志列表
     */
    public List<PermissionAuditLog> getFailedAuditLogs() {
        try {
            return permissionAuditLogRepository.findBySuccessFalse();
        } catch (Exception e) {
            log.error("查询审计日志失败", e);
            throw new ServiceException(500, "查询审计日志失败: " + e.getMessage());
        }
    }

    /**
     * 记录权限审计日志
     *
     * @param operatorId      操作人ID
     * @param operatorName    操作人名称
     * @param operationType   操作类型
     * @param description     操作描述
     * @param success         是否成功
     * @param errorMessage    错误消息
     */
    private void logAuditAction(
            BigInteger operatorId,
            String operatorName,
            String operationType,
            String description,
            boolean success,
            String errorMessage) {
        try {
            PermissionAuditLog log = new PermissionAuditLog();
            log.setOperatorId(operatorId);
            log.setOperatorName(operatorName);
            log.setOperationType(operationType);
            log.setOperationDetails(description);
            log.setOperationResult(success);
            log.setErrorMessage(errorMessage);

            permissionAuditLogRepository.insert(log);
        } catch (Exception e) {
            log.error("记录审计日志失败: operationType={}", operationType, e);
        }
    }


}
