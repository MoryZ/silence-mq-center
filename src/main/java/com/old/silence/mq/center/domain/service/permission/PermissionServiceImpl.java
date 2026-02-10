package com.old.silence.mq.center.domain.service.permission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.old.silence.mq.center.domain.model.permission.PermissionAuditLog;
import com.old.silence.mq.center.domain.model.permission.PermissionRequest;
import com.old.silence.mq.center.domain.model.permission.PermissionType;
import com.old.silence.mq.center.domain.model.permission.UserPermission;
import com.old.silence.mq.center.domain.repository.PermissionAuditLogRepository;
import com.old.silence.mq.center.domain.repository.PermissionRequestRepository;
import com.old.silence.mq.center.domain.repository.PermissionTypeRepository;
import com.old.silence.mq.center.domain.repository.TopicRepository;
import com.old.silence.mq.center.domain.repository.UserPermissionRepository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 权限服务实现类
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
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

    // ==================== 权限申请流程 ====================

    @Override
    public PermissionRequest requestPermission(BigInteger userId, String userName, BigInteger topicId, String permissionCode, String reason) {
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

            permissionRequestRepository.insert(request);

            // 记录审计日志
            recordAuditLog("REQUEST", userId, userName, userId, userName, topicId, null, permissionCode,
                    request.getId(), "SUCCESS", null);

            logger.info("权限申请已创建: requestId={}", request.getId());
            return request;

        } catch (Exception e) {
            logger.error("权限申请失败", e);
            recordAuditLog("REQUEST", userId, userName, userId, userName, topicId, null, permissionCode,
                    null, "FAILED", e.getMessage());
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserPermission approvePermission(BigInteger requestId, BigInteger approverId, String approverName,
                                            String approvalReason, LocalDateTime expireTime) {
        logger.info("审批权限申请: requestId={}, approverId={}", requestId, approverId);

        try {
            // 获取申请记录
            PermissionRequest request = permissionRequestRepository.selectById(requestId);

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
            permissionRequestRepository.insert(request);

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
                    .expired(false)
                    .status("ACTIVE")
                    .build();

            userPermissionRepository.insert(permission);

            // 记录审计日志
            recordAuditLog("APPROVE", approverId, approverName, request.getUserId(), request.getUserName(),
                    request.getTopicId(), null, request.getPermissionCode(), requestId, "SUCCESS", null);

            recordAuditLog("GRANT", approverId, approverName, request.getUserId(), request.getUserName(),
                    request.getTopicId(), null, request.getPermissionCode(), requestId, "SUCCESS", null);

            logger.info("权限已批准并授予: userId={}, permissionCode={}", request.getUserId(), request.getPermissionCode());
            return permission;

        } catch (Exception e) {
            logger.error("权限批准失败", e);
            recordAuditLog("APPROVE", approverId, approverName, null, null, null, null, null,
                    requestId, "FAILED", e.getMessage());
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void rejectPermission(BigInteger requestId, BigInteger approverId, String approverName, String rejectionReason) {
        logger.info("拒绝权限申请: requestId={}, approverId={}", requestId, approverId);

        try {
            // 获取申请记录
            PermissionRequest request = permissionRequestRepository.selectById(requestId);

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
            permissionRequestRepository.insert(request);

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
    public UserPermission grantPermission(BigInteger userId, String userName, BigInteger topicId, String permissionCode,
                                          BigInteger grantedById, String grantedByName, LocalDateTime expireTime) {
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
                    .expired(false)
                    .status("ACTIVE")
                    .build();

            userPermissionRepository.insert(permission);

            // 记录审计日志
            recordAuditLog("GRANT", grantedById, grantedByName, userId, userName, topicId, null, permissionCode,
                    null, "SUCCESS", null);

            logger.info("权限已授予: userId={}, permissionCode={}", userId, permissionCode);
            return permission;

        } catch (Exception e) {
            logger.error("权限授予失败", e);
            recordAuditLog("GRANT", grantedById, grantedByName, userId, userName, topicId, null, permissionCode,
                    null, "FAILED", e.getMessage());
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void revokePermission(BigInteger userId, BigInteger topicId, String permissionCode) {
        logger.info("撤销权限: userId={}, topicId={}, permissionCode={}", userId, topicId, permissionCode);

        try {
            // 查找权限
            Optional<UserPermission> permission = userPermissionRepository.findValidPermission(userId, topicId, permissionCode);

            if (permission.isPresent()) {
                UserPermission perm = permission.get();
                perm.setStatus("REVOKED");
                userPermissionRepository.insert(perm);

                // 记录审计日志
                recordAuditLog("REVOKE", BigInteger.ZERO, "SYSTEM", userId, perm.getUserName(), topicId, null, permissionCode,
                        null, "SUCCESS", null);

                logger.info("权限已撤销: userId={}, permissionCode={}", userId, permissionCode);
            } else {
                logger.warn("权限不存在，无需撤销: userId={}, permissionCode={}", userId, permissionCode);
            }

        } catch (Exception e) {
            logger.error("权限撤销失败", e);
            recordAuditLog("REVOKE", BigInteger.ZERO, "SYSTEM", userId, null, topicId, null, permissionCode,
                    null, "FAILED", e.getMessage());
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void expirePermission(BigInteger permissionId) {
        logger.info("标记权限为过期: permissionId={}", permissionId);

        try {
            UserPermission permission = userPermissionRepository.selectById(permissionId);

            if (permission != null) {
                permission.setStatus("EXPIRED");
                permission.setExpired(true);
                userPermissionRepository.updateById(permission);

                // 记录审计日志
                recordAuditLog("EXPIRE", BigInteger.ZERO, "SYSTEM", permission.getUserId(), permission.getUserName(), permission.getTopicId(),
                        null, permission.getPermissionCode(), null, "SUCCESS", null);

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
    public boolean hasPermission(BigInteger userId, BigInteger topicId, String permissionCode) {
        try {
            Optional<UserPermission> permission = userPermissionRepository.findValidPermission(userId, topicId, permissionCode);
            return permission.isPresent() && !permission.get().getExpired();
        } catch (Exception e) {
            logger.error("权限检查失败", e);
            return false;
        }
    }

    @Override
    public void checkPermission(BigInteger userId, BigInteger topicId, String permissionCode) throws PermissionService.PermissionDeniedException {
        if (!hasPermission(userId, topicId, permissionCode)) {
            throw new PermissionService.PermissionDeniedException(userId, topicId, permissionCode);
        }
    }

    @Override
    public boolean hasGlobalPermission(BigInteger userId, String permissionCode) {
        try {
            Optional<UserPermission> permission = userPermissionRepository.findValidGlobalPermission(userId, permissionCode);
            return permission.isPresent() && permission.get().getExpired();
        } catch (Exception e) {
            logger.error("全局权限检查失败", e);
            return false;
        }
    }

    // ==================== 权限查询方法 ====================

    @Override
    public List<UserPermission> getUserPermissions(BigInteger userId) {
        return userPermissionRepository.findValidPermissionsByUser(userId);
    }

    @Override
    public List<UserPermission> getUserPermissionsByTopic(BigInteger userId, BigInteger topicId) {
        return userPermissionRepository.findValidPermissionsByUserAndTopic(userId, topicId);
    }

    @Override
    public List<UserPermission> getTopicPermissions(BigInteger topicId) {
        return userPermissionRepository.findValidPermissionsByTopic(topicId);
    }

    @Override
    public List<PermissionRequest> getUserRequests(BigInteger userId) {
        return permissionRequestRepository.findByUserId(userId);
    }

    @Override
    public List<PermissionRequest> getPendingRequests() {
        return permissionRequestRepository.findAllPending();
    }

    @Override
    public List<PermissionAuditLog> getFailedAuditLogs() {
        return auditLogRepository.findFailedOperations();
    }

    // ==================== 辅助方法 ====================

    @Override
    public UserPermission getPermissionById(BigInteger permissionId) {
        return userPermissionRepository.selectById(permissionId);
    }

    @Override
    public PermissionRequest getRequestById(BigInteger requestId) {
        return permissionRequestRepository.selectById(requestId);
    }

    // ==================== 私有方法 ====================

    /**
     * 记录审计日志
     */
    private void recordAuditLog(String operationType, BigInteger operatorId, String operatorName,
                                BigInteger targetUserId, String targetUserName, BigInteger topicId,
                                String topicName, String permissionCode, BigInteger requestId,
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

            auditLogRepository.insert(log);
        } catch (Exception e) {
            logger.error("审计日志记录失败", e);
        }
    }
}
