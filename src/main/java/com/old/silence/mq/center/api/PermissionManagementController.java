package com.old.silence.mq.center.api;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.old.silence.mq.center.api.util.PermissionContextHolder;
import com.old.silence.mq.center.domain.model.PermissionAuditLog;
import com.old.silence.mq.center.domain.service.PermissionService;
import com.old.silence.mq.center.dto.GrantPermissionDTO;

/**
 * 权限管理 REST API 控制器
 * <p>
 * 提供权限直接赋予、撤销以及审计日志查询的 HTTP 端点
 * <p>
 * API 端点：
 * - POST /api/permission-management/grant - 直接赋予权限（管理员）
 * - DELETE /api/permission-management/revoke - 撤销权限（管理员）
 * - GET /api/permission-management/audit-logs - 获取权限审计日志
 *
 * @author Silence
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/permission-management")
public class PermissionManagementController {

    private static final Logger log = LoggerFactory.getLogger(PermissionManagementController.class);

    private final PermissionService permissionService;

    public PermissionManagementController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * 直接赋予权限（管理员操作）
     * <p>
     * 管理员直接为用户赋予权限，无需审批流程
     *
     * @param request 赋予权限请求
     */
    @PostMapping("/grant")
    public void grantPermission(@RequestBody GrantPermissionDTO request) {
        try {
            BigInteger grantedById = PermissionContextHolder.getCurrentUserId();
            String grantedByName = PermissionContextHolder.getCurrentUserName();

            log.info("管理员 {} 为用户 {} 赋予权限: topicId={}, permissionCode={}",
                    grantedById, request.getUserId(), request.getTopicId(), request.getPermissionCode());

            permissionService.grantPermission(
                    request.getUserId(),
                    request.getUserName(),
                    request.getTopicId(),
                    request.getPermissionCode(),
                    grantedById,
                    grantedByName,
                    request.getExpireTime()
            );
        } catch (Exception e) {
            log.error("赋予权限失败", e);
            throw e;
        }
    }

    /**
     * 撤销权限（管理员操作）
     * <p>
     * 管理员撤销用户的权限
     *
     * @param userId         用户 ID
     * @param topicId        Topic ID
     * @param permissionCode 权限代码
     */
    @DeleteMapping("/revoke")
    public void revokePermission(
            @RequestParam("userId") BigInteger userId,
            @RequestParam("topicId") BigInteger topicId,
            @RequestParam("permissionCode") String permissionCode) {
        try {
            log.info("管理员撤销用户 {} 在 Topic {} 上的 {} 权限", userId, topicId, permissionCode);

            permissionService.revokePermission(userId, topicId, permissionCode);
        } catch (Exception e) {
            log.error("撤销权限失败", e);
            throw e;
        }
    }

    /**
     * 获取权限审计日志
     * <p>
     * 获取所有权限操作的审计日志
     *
     * @param page 页码（默认 0）
     * @param size 每页数量（默认 50）
     * @return 审计日志列表
     */
    @GetMapping("/audit-logs")
    public List<PermissionAuditLog> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            log.debug("获取权限审计日志，page={}, size={}", page, size);

            List<PermissionAuditLog> auditLogs = permissionService.getFailedAuditLogs()
                    .stream()
                    .skip((long) page * size)
                    .limit(size)
                    .collect(Collectors.toList());

            return auditLogs;
        } catch (Exception e) {
            log.error("获取审计日志失败", e);
            throw e;
        }
    }
}
