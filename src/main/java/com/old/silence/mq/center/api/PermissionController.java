package com.old.silence.mq.center.api;

import com.old.silence.mq.center.domain.dto.*;
import com.old.silence.mq.center.domain.service.permission.PermissionService;
import com.old.silence.mq.center.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限管理 REST API 控制器
 * 
 * 提供权限申请、审批、查询等功能的 HTTP 端点
 * 
 * API 端点：
 * - POST /api/permissions/request - 申请权限
 * - POST /api/permissions/approve - 审批权限申请
 * - POST /api/permissions/reject - 拒绝权限申请
 * - POST /api/permissions/grant - 直接赋予权限（管理员）
 * - POST /api/permissions/revoke - 撤销权限（管理员）
 * - GET /api/permissions/my-permissions - 获取当前用户的权限列表
 * - GET /api/permissions/user/{userId} - 获取指定用户的权限列表
 * - GET /api/permissions/topic/{topicId} - 获取 Topic 的权限列表
 * - GET /api/permissions/requests/pending - 获取待审批权限申请
 * - GET /api/permissions/requests/user/{userId} - 获取用户的申请记录
 * - GET /api/permissions/audit-logs - 获取权限审计日志
 * 
 * @author Silence
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    // ===================== 权限申请相关 =====================

    /**
     * 申请权限
     * 
     * 用户申请对某个 Topic 的特定权限，申请会进入待审批状态
     * 需要管理员审批通过才能获得权限
     * 
     * @param request 权限申请请求
     * @return 申请结果
     */
    @PostMapping("/request")
    public ApiResponse requestPermission(@RequestBody PermissionRequestDTO request) {
        try {
            Long userId = getCurrentUserId();
            String userName = getCurrentUserName();
            
            log.info("用户 {} 申请权限: topicId={}, permissionCode={}, reason={}", 
                userId, request.getTopicId(), request.getPermissionCode(), request.getReason());
            
            // 调用权限服务申请权限
            PermissionService.PermissionRequest permissionRequest = 
                permissionService.requestPermission(
                    userId, 
                    userName, 
                    request.getTopicId(), 
                    request.getPermissionCode(), 
                    request.getReason()
                );
            
            return ApiResponse.success(
                convertToResponseDTO(permissionRequest),
                "权限申请已提交，请等待管理员审批"
            );
        } catch (Exception e) {
            log.error("权限申请失败", e);
            return ApiResponse.error(500, "权限申请失败: " + e.getMessage());
        }
    }

    // ===================== 权限审批相关 =====================

    /**
     * 审批权限申请
     * 
     * 管理员同意申请者的权限申请，会为申请者创建有效期内的权限
     * 
     * @param request 审批请求
     * @return 审批结果
     */
    @PostMapping("/approve")
    public ApiResponse approvePermission(@RequestBody ApprovePermissionDTO request) {
        try {
            Long approverId = getCurrentUserId();
            String approverName = getCurrentUserName();
            
            log.info("管理员 {} 审批权限申请 requestId={}", approverId, request.getRequestId());
            
            // 调用权限服务审批
            permissionService.approvePermission(
                request.getRequestId(),
                approverId,
                approverName,
                request.getApprovalReason(),
                request.getExpireTime()
            );
            
            return ApiResponse.success("权限申请已通过，权限已赋予申请者");
        } catch (PermissionService.PermissionDeniedException e) {
            log.warn("审批权限申请被拒绝: {}", e.getMessage());
            return ApiResponse.error(403, "您无权执行此操作: " + e.getMessage());
        } catch (Exception e) {
            log.error("审批权限申请失败", e);
            return ApiResponse.error(500, "审批失败: " + e.getMessage());
        }
    }

    /**
     * 拒绝权限申请
     * 
     * 管理员拒绝申请者的权限申请，申请会进入拒绝状态
     * 
     * @param request 拒绝请求
     * @return 拒绝结果
     */
    @PostMapping("/reject")
    public ApiResponse rejectPermission(@RequestBody RejectPermissionDTO request) {
        try {
            Long approverId = getCurrentUserId();
            String approverName = getCurrentUserName();
            
            log.info("管理员 {} 拒绝权限申请 requestId={}", approverId, request.getRequestId());
            
            // 调用权限服务拒绝
            permissionService.rejectPermission(
                request.getRequestId(),
                approverId,
                approverName,
                request.getRejectionReason()
            );
            
            return ApiResponse.success("权限申请已拒绝");
        } catch (Exception e) {
            log.error("拒绝权限申请失败", e);
            return ApiResponse.error(500, "操作失败: " + e.getMessage());
        }
    }

    // ===================== 权限直接赋予相关（管理员操作）=====================

    /**
     * 直接赋予权限（管理员操作）
     * 
     * 管理员直接为用户赋予权限，无需审批流程
     * 
     * @param request 赋予权限请求
     * @return 赋予结果
     */
    @PostMapping("/grant")
    public ApiResponse grantPermission(@RequestBody GrantPermissionDTO request) {
        try {
            Long grantedById = getCurrentUserId();
            String grantedByName = getCurrentUserName();
            
            log.info("管理员 {} 为用户 {} 赋予权限: topicId={}, permissionCode={}", 
                grantedById, request.getUserId(), request.getTopicId(), request.getPermissionCode());
            
            // 调用权限服务直接赋予
            permissionService.grantPermission(
                request.getUserId(),
                request.getUserName(),
                request.getTopicId(),
                request.getPermissionCode(),
                grantedById,
                grantedByName,
                request.getExpireTime()
            );
            
            return ApiResponse.success("权限赋予成功");
        } catch (Exception e) {
            log.error("赋予权限失败", e);
            return ApiResponse.error(500, "赋予权限失败: " + e.getMessage());
        }
    }

    /**
     * 撤销权限（管理员操作）
     * 
     * 管理员撤销用户的权限
     * 
     * @param userId 用户 ID
     * @param topicId Topic ID
     * @param permissionCode 权限代码
     * @return 撤销结果
     */
    @PostMapping("/revoke")
    public ApiResponse revokePermission(
            @RequestParam("userId") Long userId,
            @RequestParam("topicId") Long topicId,
            @RequestParam("permissionCode") String permissionCode) {
        try {
            log.info("管理员撤销用户 {} 在 Topic {} 上的 {} 权限", userId, topicId, permissionCode);
            
            // 调用权限服务撤销
            permissionService.revokePermission(userId, topicId, permissionCode);
            
            return ApiResponse.success("权限已撤销");
        } catch (Exception e) {
            log.error("撤销权限失败", e);
            return ApiResponse.error(500, "撤销权限失败: " + e.getMessage());
        }
    }

    // ===================== 权限查询相关 =====================

    /**
     * 获取当前用户的权限列表
     * 
     * 返回当前认证用户在所有 Topic 上的有效权限
     * 
     * @return 用户权限列表
     */
    @GetMapping("/my-permissions")
    public ApiResponse getMyPermissions() {
        try {
            Long userId = getCurrentUserId();
            log.debug("获取用户 {} 的权限列表", userId);
            
            // 调用权限服务查询
            List<UserPermissionDTO> permissions = permissionService.getUserPermissions(userId)
                .stream()
                .map(this::convertToUserPermissionDTO)
                .collect(Collectors.toList());
            
            return ApiResponse.success(permissions, "权限列表获取成功");
        } catch (Exception e) {
            log.error("获取权限列表失败", e);
            return ApiResponse.error(500, "获取权限列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定用户的权限列表
     * 
     * 管理员可以查询任意用户的权限列表
     * 
     * @param userId 用户 ID
     * @return 用户权限列表
     */
    @GetMapping("/user/{userId}")
    public ApiResponse getUserPermissions(@PathVariable Long userId) {
        try {
            log.debug("获取用户 {} 的权限列表", userId);
            
            // 调用权限服务查询
            List<UserPermissionDTO> permissions = permissionService.getUserPermissions(userId)
                .stream()
                .map(this::convertToUserPermissionDTO)
                .collect(Collectors.toList());
            
            return ApiResponse.success(permissions, "权限列表获取成功");
        } catch (Exception e) {
            log.error("获取权限列表失败", e);
            return ApiResponse.error(500, "获取权限列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定 Topic 的权限列表
     * 
     * 获取所有用户在某个 Topic 上的权限
     * 
     * @param topicId Topic ID
     * @return Topic 权限列表
     */
    @GetMapping("/topic/{topicId}")
    public ApiResponse getTopicPermissions(@PathVariable Long topicId) {
        try {
            log.debug("获取 Topic {} 的权限列表", topicId);
            
            // 调用权限服务查询
            List<UserPermissionDTO> permissions = permissionService.getTopicPermissions(topicId)
                .stream()
                .map(this::convertToUserPermissionDTO)
                .collect(Collectors.toList());
            
            return ApiResponse.success(permissions, "权限列表获取成功");
        } catch (Exception e) {
            log.error("获取权限列表失败", e);
            return ApiResponse.error(500, "获取权限列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户在指定 Topic 上的权限
     * 
     * @param userId 用户 ID
     * @param topicId Topic ID
     * @return 用户在该 Topic 上的权限列表
     */
    @GetMapping("/user/{userId}/topic/{topicId}")
    public ApiResponse getUserTopicPermissions(
            @PathVariable Long userId,
            @PathVariable Long topicId) {
        try {
            log.debug("获取用户 {} 在 Topic {} 上的权限", userId, topicId);
            
            // 调用权限服务查询
            List<UserPermissionDTO> permissions = permissionService.getUserPermissionsByTopic(userId, topicId)
                .stream()
                .map(this::convertToUserPermissionDTO)
                .collect(Collectors.toList());
            
            return ApiResponse.success(permissions, "权限列表获取成功");
        } catch (Exception e) {
            log.error("获取权限列表失败", e);
            return ApiResponse.error(500, "获取权限列表失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户是否有权限
     * 
     * @param userId 用户 ID
     * @param topicId Topic ID（为 null 表示检查全局权限）
     * @param permissionCode 权限代码
     * @return 权限检查结果
     */
    @GetMapping("/check")
    public ApiResponse checkPermission(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "topicId", required = false) Long topicId,
            @RequestParam("permissionCode") String permissionCode) {
        try {
            log.debug("检查用户 {} 是否有权限 {} 在 Topic {}", userId, permissionCode, topicId);
            
            // 调用权限服务检查
            boolean hasPermission = permissionService.hasPermission(userId, topicId, permissionCode);
            
            return ApiResponse.success(
                new CheckPermissionResponseDTO(userId, topicId, permissionCode, hasPermission),
                "权限检查完成"
            );
        } catch (Exception e) {
            log.error("权限检查失败", e);
            return ApiResponse.error(500, "权限检查失败: " + e.getMessage());
        }
    }

    // ===================== 权限申请记录查询 =====================

    /**
     * 获取待审批的权限申请列表
     * 
     * 管理员查询所有待审批的权限申请
     * 
     * @param page 页码（默认 1）
     * @param size 每页数量（默认 20）
     * @return 待审批申请列表
     */
    @GetMapping("/requests/pending")
    public ApiResponse getPendingRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.debug("获取待审批权限申请列表，page={}, size={}", page, size);
            
            // 调用权限服务查询
            List<PermissionRequestResponseDTO> requests = permissionService.getPendingRequests()
                .stream()
                .skip((long) page * size)
                .limit(size)
                .map(this::convertToRequestResponseDTO)
                .collect(Collectors.toList());
            
            return ApiResponse.success(requests, "待审批申请列表获取成功");
        } catch (Exception e) {
            log.error("获取待审批申请失败", e);
            return ApiResponse.error(500, "获取待审批申请失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的权限申请记录
     * 
     * 获取指定用户提交的所有权限申请记录
     * 
     * @param userId 用户 ID
     * @param page 页码（默认 0）
     * @param size 每页数量（默认 20）
     * @return 用户的申请记录列表
     */
    @GetMapping("/requests/user/{userId}")
    public ApiResponse getUserRequests(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.debug("获取用户 {} 的权限申请记录", userId);
            
            // 调用权限服务查询
            List<PermissionRequestResponseDTO> requests = permissionService.getUserRequests(userId)
                .stream()
                .skip((long) page * size)
                .limit(size)
                .map(this::convertToRequestResponseDTO)
                .collect(Collectors.toList());
            
            return ApiResponse.success(requests, "申请记录获取成功");
        } catch (Exception e) {
            log.error("获取申请记录失败", e);
            return ApiResponse.error(500, "获取申请记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取权限审计日志
     * 
     * 获取所有权限操作的审计日志
     * 
     * @param page 页码（默认 0）
     * @param size 每页数量（默认 50）
     * @return 审计日志列表
     */
    @GetMapping("/audit-logs")
    public ApiResponse getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            log.debug("获取权限审计日志，page={}, size={}", page, size);
            
            // 调用权限服务查询
            List<PermissionService.AuditLog> auditLogs = permissionService.getFailedAuditLogs()
                .stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
            
            return ApiResponse.success(auditLogs, "审计日志获取成功");
        } catch (Exception e) {
            log.error("获取审计日志失败", e);
            return ApiResponse.error(500, "获取审计日志失败: " + e.getMessage());
        }
    }

    // ===================== 辅助方法 =====================

    /**
     * 获取当前用户 ID
     * 
     * @return 用户 ID
     * @throws ServiceException 如果无法获取用户 ID
     */
    private Long getCurrentUserId() {
        try {
            // 尝试从 Spring Security 获取
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();
                if (principal instanceof Long) {
                    return (Long) principal;
                }
                if (principal instanceof UserInfo) {
                    return ((UserInfo) principal).getId();
                }
            }
            throw new ServiceException("无法获取当前用户信息，请先登录");
        } catch (Exception e) {
            throw new ServiceException("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户名称
     * 
     * @return 用户名称
     */
    private String getCurrentUserName() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                Object principal = auth.getPrincipal();
                if (principal instanceof UserInfo) {
                    return ((UserInfo) principal).getUsername();
                }
                return auth.getName();
            }
            return "UNKNOWN";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    /**
     * 权限信息接口（用于 SecurityContext）
     */
    public interface UserInfo {
        Long getId();
        String getUsername();
    }

    // ===================== DTO 转换方法 =====================

    private PermissionRequestResponseDTO convertToRequestResponseDTO(PermissionService.PermissionRequest request) {
        PermissionRequestResponseDTO dto = new PermissionRequestResponseDTO();
        dto.setRequestId(request.getId());
        dto.setUserId(request.getUserId());
        dto.setUserName(request.getUserName());
        dto.setTopicId(request.getTopicId());
        dto.setPermissionCode(request.getPermissionCode());
        dto.setReason(request.getReason());
        dto.setStatus(request.getStatus().name());
        dto.setRequestTime(request.getRequestTime());
        return dto;
    }

    private UserPermissionDTO convertToUserPermissionDTO(PermissionService.UserPermission permission) {
        UserPermissionDTO dto = new UserPermissionDTO();
        dto.setPermissionId(permission.getId());
        dto.setUserId(permission.getUserId());
        dto.setUserName(permission.getUserName());
        dto.setTopicId(permission.getTopicId());
        dto.setPermissionCode(permission.getPermissionCode());
        dto.setStatus(permission.getStatus().name());
        dto.setExpireTime(permission.getExpireTime());
        dto.setGrantedTime(permission.getGrantedTime());
        dto.setExpired(permission.isExpired());
        return dto;
    }

    /**
     * 权限检查响应 DTO
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class CheckPermissionResponseDTO {
        private Long userId;
        private Long topicId;
        private String permissionCode;
        private boolean hasPermission;
    }
}
