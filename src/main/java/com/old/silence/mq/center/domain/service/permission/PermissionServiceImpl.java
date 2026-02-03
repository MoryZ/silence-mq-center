package com.old.silence.mq.center.domain.service.permission;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.old.silence.mq.center.domain.model.permission.*;
import com.old.silence.mq.center.domain.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.common.base.Throwables;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 权限服务实现类
 */
@Service
@Transactional
public class PermissionServiceImpl implements PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);

    @Autowired
    private PermissionTypeRepository permissionTypeRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private PermissionRequestRepository permissionRequestRepository;

    @Autowired
    private UserPermissionRepository userPermissionRepository;

    @Autowired
    private PermissionAuditLogRepository auditLogRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== 权限申请流程 ====================

    @Override
    public PermissionRequest requestPermission(Long userId, String userName, Long topicId, String permissionCode, String reason) {
        logger.info("用户 {} 申请权限: topicId={}, permissionCode={}", userId, topicId, permissionCode);

        try {
            // 获取权限类型
            PermissionType permissionType = permissionTypeRepository.findByPermissionCode(permissionCode)
                .orElseThrow(() -> new IllegalArgumentException("权限类型不存在: " + permissionCode));

            // 创建申请记录
            PermissionRequest request = PermissionRequest.builder()
                .userId(userId)
                .userName(userName)
                .topicId(topicId)
                .permissionTypeId(permissionType.getId())
                .permissionCode(permissionCode)
                .requestReason(reason)
                .status("PENDING")
                .build();

            PermissionRequest savedRequest = permissionRequestRepository.save(request);

            // 记录审计日志
            recordAuditLog("REQUEST", userId, userName, userId, userName, topicId, null, permissionCode, 
                          savedRequest.getId(), "SUCCESS", null);

            logger.info("权限申请已创建: requestId={}", savedRequest.getId());
            return savedRequest;

        } catch (Exception e) {
            logger.error("权限申请失败", e);
            recordAuditLog("REQUEST", userId, userName, userId, userName, topicId, null, permissionCode, 
                          null, "FAILED", e.getMessage());
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserPermission approvePermission(Long requestId, Long approverId, String approverName, 
                                           String approvalReason, LocalDateTime expireTime) {
        logger.info("审批权限申请: requestId={}, approverId={}", requestId, approverId);

        try {
            // 获取申请记录
            PermissionRequest request = permissionRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("申请不存在: " + requestId));

            // 验证申请状态
            if (!"PENDING".equals(request.getStatus())) {
                throw new IllegalStateException("申请状态不是待审批: " + request.getStatus());
            }

            // 更新申请状态
            request.setStatus("APPROVED");
            request.setApproverId(approverId);
            request.setApproverName(approverName);
            request.setApprovalReason(approvalReason);
            request.setApprovalTime(LocalDateTime.now());
            request.setExpireTime(expireTime);
            permissionRequestRepository.save(request);

            // 授予权限
            UserPermission permission = UserPermission.builder()
                .userId(request.getUserId())
                .userName(request.getUserName())
                .topicId(request.getTopicId())
                .topicName(request.getTopicName())
                .permissionTypeId(request.getPermissionTypeId())
                .permissionCode(request.getPermissionCode())
                .grantedById(approverId)
                .grantedByName(approverName)
                .grantedTime(LocalDateTime.now())
                .expireTime(expireTime)
                .isExpired(0)
                .status("ACTIVE")
                .build();

            UserPermission savedPermission = userPermissionRepository.save(permission);

            // 记录审计日志
            recordAuditLog("APPROVE", approverId, approverName, request.getUserId(), request.getUserName(), 
                          request.getTopicId(), null, request.getPermissionCode(), requestId, "SUCCESS", null);

            recordAuditLog("GRANT", approverId, approverName, request.getUserId(), request.getUserName(), 
                          request.getTopicId(), null, request.getPermissionCode(), requestId, "SUCCESS", null);

            logger.info("权限已批准并授予: userId={}, permissionCode={}", request.getUserId(), request.getPermissionCode());
            return savedPermission;

        } catch (Exception e) {
            logger.error("权限批准失败", e);
            recordAuditLog("APPROVE", approverId, approverName, null, null, null, null, null, 
                          requestId, "FAILED", e.getMessage());
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void rejectPermission(Long requestId, Long approverId, String approverName, String rejectionReason) {
        logger.info("拒绝权限申请: requestId={}, approverId={}", requestId, approverId);

        try {
            // 获取申请记录
            PermissionRequest request = permissionRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("申请不存在: " + requestId));

            // 验证申请状态
            if (!"PENDING".equals(request.getStatus())) {
                throw new IllegalStateException("申请状态不是待审批: " + request.getStatus());
            }

            // 更新申请状态
            request.setStatus("REJECTED");
            request.setApproverId(approverId);
            request.setApproverName(approverName);
            request.setApprovalReason(rejectionReason);
            request.setApprovalTime(LocalDateTime.now());
            permissionRequestRepository.save(request);

            // 记录审计日志
            recordAuditLog("REJECT", approverId, approverName, request.getUserId(), request.getUserName(), 
                          request.getTopicId(), null, request.getPermissionCode(), requestId, "SUCCESS", null);

            logger.info("权限申请已拒绝: requestId={}", requestId);

        } catch (Exception e) {
            logger.error("拒绝权限申请失败", e);
            recordAuditLog("REJECT", approverId, approverName, null, null, null, null, null, 
                          requestId, "FAILED", e.getMessage());
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserPermission grantPermission(Long userId, String userName, Long topicId, String permissionCode, 
                                         Long grantedById, String grantedByName, LocalDateTime expireTime) {
        logger.info("直接授予权限: userId={}, topicId={}, permissionCode={}", userId, topicId, permissionCode);

        try {
            // 获取权限类型
            PermissionType permissionType = permissionTypeRepository.findByPermissionCode(permissionCode)
                .orElseThrow(() -> new IllegalArgumentException("权限类型不存在: " + permissionCode));

            // 创建权限记录
            UserPermission permission = UserPermission.builder()
                .userId(userId)
                .userName(userName)
                .topicId(topicId)
                .permissionTypeId(permissionType.getId())
                .permissionCode(permissionCode)
                .grantedById(grantedById)
                .grantedByName(grantedByName)
                .grantedTime(LocalDateTime.now())
                .expireTime(expireTime)
                .isExpired(0)
                .status("ACTIVE")
                .build();

            UserPermission savedPermission = userPermissionRepository.save(permission);

            // 记录审计日志
            recordAuditLog("GRANT", grantedById, grantedByName, userId, userName, topicId, null, permissionCode, 
                          null, "SUCCESS", null);

            logger.info("权限已授予: userId={}, permissionCode={}", userId, permissionCode);
            return savedPermission;

        } catch (Exception e) {
            logger.error("权限授予失败", e);
            recordAuditLog("GRANT", grantedById, grantedByName, userId, userName, topicId, null, permissionCode, 
                          null, "FAILED", e.getMessage());
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void revokePermission(Long userId, Long topicId, String permissionCode) {
        logger.info("撤销权限: userId={}, topicId={}, permissionCode={}", userId, topicId, permissionCode);

        try {
            // 查找权限
            Optional<UserPermission> permission = userPermissionRepository.findValidPermission(userId, topicId, permissionCode);

            if (permission.isPresent()) {
                UserPermission perm = permission.get();
                perm.setStatus("REVOKED");
                userPermissionRepository.save(perm);

                // 记录审计日志
                recordAuditLog("REVOKE", 0L, "SYSTEM", userId, perm.getUserName(), topicId, null, permissionCode, 
                              null, "SUCCESS", null);

                logger.info("权限已撤销: userId={}, permissionCode={}", userId, permissionCode);
            } else {
                logger.warn("权限不存在，无需撤销: userId={}, permissionCode={}", userId, permissionCode);
            }

        } catch (Exception e) {
            logger.error("权限撤销失败", e);
            recordAuditLog("REVOKE", 0L, "SYSTEM", userId, null, topicId, null, permissionCode, 
                          null, "FAILED", e.getMessage());
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void expirePermission(Long permissionId) {
        logger.info("标记权限为过期: permissionId={}", permissionId);

        try {
            Optional<UserPermission> permission = userPermissionRepository.findById(permissionId);

            if (permission.isPresent()) {
                UserPermission perm = permission.get();
                perm.setStatus("EXPIRED");
                perm.setIsExpired(1);
                userPermissionRepository.save(perm);

                // 记录审计日志
                recordAuditLog("EXPIRE", 0L, "SYSTEM", perm.getUserId(), perm.getUserName(), perm.getTopicId(), 
                              null, perm.getPermissionCode(), null, "SUCCESS", null);

                logger.info("权限已标记为过期: permissionId={}", permissionId);
            }

        } catch (Exception e) {
            logger.error("权限过期处理失败", e);
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public int expireExpiredPermissions() {
        logger.info("自动处理所有过期权限");

        try {
            List<UserPermission> expiredPermissions = userPermissionRepository.findExpiredPermissions();
            int count = 0;

            for (UserPermission perm : expiredPermissions) {
                if (perm.getExpireTime() != null && LocalDateTime.now().isAfter(perm.getExpireTime())) {
                    expirePermission(perm.getId());
                    count++;
                }
            }

            logger.info("已处理 {} 个过期权限", count);
            return count;

        } catch (Exception e) {
            logger.error("自动处理过期权限失败", e);
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    // ==================== 权限检查方法 ====================

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, Long topicId, String permissionCode) {
        try {
            Optional<UserPermission> permission = userPermissionRepository.findValidPermission(userId, topicId, permissionCode);
            return permission.isPresent() && permission.get().isValid();
        } catch (Exception e) {
            logger.error("权限检查失败", e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void checkPermission(Long userId, Long topicId, String permissionCode) throws PermissionService.PermissionDeniedException {
        if (!hasPermission(userId, topicId, permissionCode)) {
            throw new PermissionService.PermissionDeniedException(userId, topicId, permissionCode);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasGlobalPermission(Long userId, String permissionCode) {
        try {
            Optional<UserPermission> permission = userPermissionRepository.findValidGlobalPermission(userId, permissionCode);
            return permission.isPresent() && permission.get().isValid();
        } catch (Exception e) {
            logger.error("全局权限检查失败", e);
            return false;
        }
    }

    // ==================== 权限查询方法 ====================

    @Override
    @Transactional(readOnly = true)
    public List<UserPermission> getUserPermissions(Long userId) {
        return userPermissionRepository.findValidPermissionsByUser(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserPermission> getUserPermissionsByTopic(Long userId, Long topicId) {
        return userPermissionRepository.findValidPermissionsByUserAndTopic(userId, topicId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserPermission> getTopicPermissions(Long topicId) {
        return userPermissionRepository.findValidPermissionsByTopic(topicId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionRequest> getUserRequests(Long userId) {
        return permissionRequestRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionRequest> getPendingRequests() {
        return permissionRequestRepository.findAllPending();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionAuditLog> getFailedAuditLogs() {
        return auditLogRepository.findFailedOperations();
    }

    // ==================== 辅助方法 ====================

    @Override
    @Transactional(readOnly = true)
    public Optional<UserPermission> getPermissionById(Long permissionId) {
        return userPermissionRepository.findById(permissionId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PermissionRequest> getRequestById(Long requestId) {
        return permissionRequestRepository.findById(requestId);
    }

    // ==================== 私有方法 ====================

    /**
     * 记录审计日志
     */
    private void recordAuditLog(String operationType, Long operatorId, String operatorName, 
                               Long targetUserId, String targetUserName, Long topicId, 
                               String topicName, String permissionCode, Long requestId, 
                               String result, String errorMessage) {
        try {
            Map<String, Object> details = new HashMap<>();
            details.put("timestamp", LocalDateTime.now());
            details.put("operationType", operationType);

            PermissionAuditLog log = PermissionAuditLog.builder()
                .operationType(operationType)
                .operatorId(operatorId)
                .operatorName(operatorName)
                .targetUserId(targetUserId)
                .targetUserName(targetUserName)
                .topicId(topicId)
                .topicName(topicName)
                .permissionCode(permissionCode)
                .requestId(requestId)
                .operationDetails(objectMapper.writeValueAsString(details))
                .operationResult(result)
                .errorMessage(errorMessage)
                .build();

            auditLogRepository.save(log);
        } catch (Exception e) {
            logger.error("审计日志记录失败", e);
        }
    }
}
