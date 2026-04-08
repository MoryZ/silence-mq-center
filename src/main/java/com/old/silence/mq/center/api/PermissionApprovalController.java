package com.old.silence.mq.center.api;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.old.silence.mq.center.api.util.PermissionContextHolder;
import com.old.silence.mq.center.domain.service.PermissionService;
import com.old.silence.mq.center.dto.ApprovePermissionDTO;
import com.old.silence.mq.center.dto.RejectPermissionDTO;
import com.old.silence.mq.center.exception.PermissionDeniedException;

/**
 * 权限审批管理 REST API 控制器
 * <p>
 * 提供权限申请审批功能的 HTTP 端点
 * <p>
 * API 端点：
 * - POST /api/permission-approvals/approve - 审批权限申请（通过）
 * - POST /api/permission-approvals/reject - 审批权限申请（拒绝）
 *
 * @author Silence
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/permission-approvals")
public class PermissionApprovalController {

    private static final Logger log = LoggerFactory.getLogger(PermissionApprovalController.class);

    private final PermissionService permissionService;

    public PermissionApprovalController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * 审批权限申请（通过）
     * <p>
     * 管理员同意申请者的权限申请，会为申请者创建有效期内的权限
     *
     * @param request 审批请求
     */
    @PostMapping("/approve")
    public void approvePermission(@RequestBody ApprovePermissionDTO request) {
        try {
            BigInteger approverId = PermissionContextHolder.getCurrentUserId();
            String approverName = PermissionContextHolder.getCurrentUserName();

            log.info("管理员 {} 审批权限申请通过 requestId={}", approverId, request.getRequestId());

            permissionService.approvePermission(
                    request.getRequestId(),
                    approverId,
                    approverName,
                    request.getApprovalReason(),
                    request.getExpireTime()
            );
        } catch (PermissionDeniedException e) {
            log.warn("审批权限申请被拒绝: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("审批权限申请失败", e);
            throw e;
        }
    }

    /**
     * 拒绝权限申请
     * <p>
     * 管理员拒绝申请者的权限申请，申请会进入拒绝状态
     *
     * @param request 拒绝请求
     */
    @PostMapping("/reject")
    public void rejectPermission(@RequestBody RejectPermissionDTO request) {
        try {
            BigInteger approverId = PermissionContextHolder.getCurrentUserId();
            String approverName = PermissionContextHolder.getCurrentUserName();

            log.info("管理员 {} 拒绝权限申请 requestId={}", approverId, request.getRequestId());

            permissionService.rejectPermission(
                    request.getRequestId(),
                    approverId,
                    approverName,
                    request.getRejectionReason()
            );
        } catch (Exception e) {
            log.error("拒绝权限申请失败", e);
            throw e;
        }
    }
}
