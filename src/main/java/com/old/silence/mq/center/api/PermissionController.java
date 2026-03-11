package com.old.silence.mq.center.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.old.silence.mq.center.domain.dto.GrantPermissionDTO;
import com.old.silence.mq.center.domain.model.permission.PermissionAuditLog;
import com.old.silence.mq.center.domain.model.permission.PermissionRequest;
import com.old.silence.mq.center.domain.model.permission.UserPermission;
import com.old.silence.mq.center.domain.model.permission.dto.ApiResponse;
import com.old.silence.mq.center.domain.model.permission.dto.ApprovePermissionDTO;
import com.old.silence.mq.center.domain.model.permission.dto.PermissionRequestDTO;
import com.old.silence.mq.center.domain.model.permission.dto.PermissionRequestResponseDTO;
import com.old.silence.mq.center.domain.model.permission.dto.RejectPermissionDTO;
import com.old.silence.mq.center.domain.model.permission.dto.UserPermissionDTO;
import com.old.silence.mq.center.domain.service.permission.PermissionService;
import com.old.silence.mq.center.exception.ServiceException;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限管理 REST API 控制器
 * <p>
 * 提供权限申请、审批、查询等功能的 HTTP 端点
 * <p>
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
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private static final Logger log = LoggerFactory.getLogger(PermissionController.class);

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    // ===================== 权限申请相关 =====================

    /**
     * 申请权限
     * <p>
     * 用户申请对某个 Topic 的特定权限，申请会进入待审批状态
     * 需要管理员审批通过才能获得权限
     *
     * @param request 权限申请请求
     * @return 申请结果
     */
    @PostMapping("/request")
    public ApiResponse requestPermission(@RequestBody PermissionRequestDTO request) {
        try {
            BigInteger userId = getCurrentUserId();
            String userName = getCurrentUserName();

            log.info("用户 {} 申请权限: topicId={}, permissionCode={}, reason={}",
                    userId, request.getTopicId(), request.getPermissionCode(), request.getReason());

            // 调用权限服务申请权限
            PermissionRequest permissionRequest =
                    permissionService.requestPermission(
                            userId,
                            userName,
                            request.getTopicId(),
                            request.getPermissionCode(),
                            request.getReason()
                    );

            return ApiResponse.success(permissionRequest);
        } catch (Exception e) {
            log.error("权限申请失败", e);
            return ApiResponse.error(500, "权限申请失败: " + e.getMessage());
        }
    }

    // ===================== 权限审批相关 =====================

    /**
     * 审批权限申请
     * <p>
     * 管理员同意申请者的权限申请，会为申请者创建有效期内的权限
     *
     * @param request 审批请求
     * @return 审批结果
     */
    @PostMapping("/approve")
    public ApiResponse approvePermission(@RequestBody ApprovePermissionDTO request) {
        try {
            BigInteger approverId = getCurrentUserId();
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
     * <p>
     * 管理员拒绝申请者的权限申请，申请会进入拒绝状态
     *
     * @param request 拒绝请求
     * @return 拒绝结果
     */
    @PostMapping("/reject")
    public ApiResponse rejectPermission(@RequestBody RejectPermissionDTO request) {
        try {
            BigInteger approverId = getCurrentUserId();
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
     * <p>
     * 管理员直接为用户赋予权限，无需审批流程
     *
     * @param request 赋予权限请求
     * @return 赋予结果
     */
    @PostMapping("/grant")
    public ApiResponse grantPermission(@RequestBody GrantPermissionDTO request) {
        try {
            BigInteger grantedById = getCurrentUserId();
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
     * <p>
     * 管理员撤销用户的权限
     *
     * @param userId         用户 ID
     * @param topicId        Topic ID
     * @param permissionCode 权限代码
     * @return 撤销结果
     */
    @PostMapping("/revoke")
    public ApiResponse revokePermission(
            @RequestParam("userId") BigInteger userId,
            @RequestParam("topicId") BigInteger topicId,
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
     * <p>
     * 返回当前认证用户在所有 Topic 上的有效权限
     *
     * @return 用户权限列表
     */
    @GetMapping("/my-permissions")
    public ApiResponse getMyPermissions() {
        try {
            BigInteger userId = getCurrentUserId();
            log.debug("获取用户 {} 的权限列表", userId);

            // 调用权限服务查询
            List<UserPermissionDTO> permissions = permissionService.getUserPermissions(userId)
                    .stream()
                    .map(this::convertToUserPermissionDTO)
                    .collect(Collectors.toList());

            return ApiResponse.success(permissions);
        } catch (Exception e) {
            log.error("获取权限列表失败", e);
            return ApiResponse.error(500, "获取权限列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定用户的权限列表
     * <p>
     * 管理员可以查询任意用户的权限列表
     *
     * @param userId 用户 ID
     * @return 用户权限列表
     */
    @GetMapping("/user/{userId}")
    public ApiResponse getUserPermissions(@PathVariable BigInteger userId) {
        try {
            log.debug("获取用户 {} 的权限列表", userId);

            // 调用权限服务查询
            List<UserPermissionDTO> permissions = permissionService.getUserPermissions(userId)
                    .stream()
                    .map(this::convertToUserPermissionDTO)
                    .collect(Collectors.toList());

            return ApiResponse.success(permissions);
        } catch (Exception e) {
            log.error("获取权限列表失败", e);
            return ApiResponse.error(500, "获取权限列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定 Topic 的权限列表
     * <p>
     * 获取所有用户在某个 Topic 上的权限
     *
     * @param topicId Topic ID
     * @return Topic 权限列表
     */
    @GetMapping("/topic/{topicId}")
    public ApiResponse getTopicPermissions(@PathVariable BigInteger topicId) {
        try {
            log.debug("获取 Topic {} 的权限列表", topicId);

            // 调用权限服务查询
            List<UserPermissionDTO> permissions = permissionService.getTopicPermissions(topicId)
                    .stream()
                    .map(this::convertToUserPermissionDTO)
                    .collect(Collectors.toList());

            return ApiResponse.success(permissions);
        } catch (Exception e) {
            log.error("获取权限列表失败", e);
            return ApiResponse.error(500, "获取权限列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户在指定 Topic 上的权限
     *
     * @param userId  用户 ID
     * @param topicId Topic ID
     * @return 用户在该 Topic 上的权限列表
     */
    @GetMapping("/user/{userId}/topic/{topicId}")
    public ApiResponse getUserTopicPermissions(
            @PathVariable BigInteger userId,
            @PathVariable BigInteger topicId) {
        try {
            log.debug("获取用户 {} 在 Topic {} 上的权限", userId, topicId);

            // 调用权限服务查询
            List<UserPermissionDTO> permissions = permissionService.getUserPermissionsByTopic(userId, topicId)
                    .stream()
                    .map(this::convertToUserPermissionDTO)
                    .collect(Collectors.toList());

            return ApiResponse.success(permissions);
        } catch (Exception e) {
            log.error("获取权限列表失败", e);
            return ApiResponse.error(500, "获取权限列表失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户是否有权限
     *
     * @param userId         用户 ID
     * @param topicId        Topic ID（为 null 表示检查全局权限）
     * @param permissionCode 权限代码
     * @return 权限检查结果
     */
    @GetMapping("/check")
    public ApiResponse checkPermission(
            @RequestParam("userId") BigInteger userId,
            @RequestParam(value = "topicId", required = false) BigInteger topicId,
            @RequestParam("permissionCode") String permissionCode) {
        try {
            log.debug("检查用户 {} 是否有权限 {} 在 Topic {}", userId, permissionCode, topicId);

            // 调用权限服务检查
            boolean hasPermission = permissionService.hasPermission(userId, topicId, permissionCode);

            return ApiResponse.success(
                    new CheckPermissionResponseDTO(userId, topicId, permissionCode, hasPermission)
            );
        } catch (Exception e) {
            log.error("权限检查失败", e);
            return ApiResponse.error(500, "权限检查失败: " + e.getMessage());
        }
    }

    // ===================== 权限申请记录查询 =====================

    /**
     * 获取待审批的权限申请列表
     * <p>
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

            return ApiResponse.success(requests);
        } catch (Exception e) {
            log.error("获取待审批申请失败", e);
            return ApiResponse.error(500, "获取待审批申请失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的权限申请记录
     * <p>
     * 获取指定用户提交的所有权限申请记录
     *
     * @param userId 用户 ID
     * @param page   页码（默认 0）
     * @param size   每页数量（默认 20）
     * @return 用户的申请记录列表
     */
    @GetMapping("/requests/user/{userId}")
    public ApiResponse getUserRequests(
            @PathVariable BigInteger userId,
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

            return ApiResponse.success(requests);
        } catch (Exception e) {
            log.error("获取申请记录失败", e);
            return ApiResponse.error(500, "获取申请记录失败: " + e.getMessage());
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
    public ApiResponse getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            log.debug("获取权限审计日志，page={}, size={}", page, size);

            // 调用权限服务查询
            List<PermissionAuditLog> auditLogs = permissionService.getFailedAuditLogs()
                    .stream()
                    .skip((long) page * size)
                    .limit(size)
                    .collect(Collectors.toList());

            return ApiResponse.success(auditLogs);
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
    private BigInteger getCurrentUserId() {
        try {
            // 从 HttpServletRequest 的 Header 中获取用户 ID
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String userId = request.getHeader("X-User-Id");
                if (userId != null && !userId.isEmpty()) {
                    return new BigInteger(userId);
                }
            }
            throw new ServiceException(500, "无法获取当前用户信息，请在请求头中传入 X-User-Id");
        } catch (NumberFormatException e) {
            throw new ServiceException(500, "用户 ID 格式不正确");
        } catch (Exception e) {
            throw new ServiceException(500, "获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户名称
     *
     * @return 用户名称
     */
    private String getCurrentUserName() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String userName = request.getHeader("X-User-Name");
                if (userName != null && !userName.isEmpty()) {
                    return userName;
                }
            }
            return "UNKNOWN";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private PermissionRequestResponseDTO convertToRequestResponseDTO(PermissionRequest request) {
        PermissionRequestResponseDTO dto = new PermissionRequestResponseDTO();
        dto.setRequestId(request.getId());
        dto.setUserId(request.getUserId());
        dto.setUserName(request.getUserName());
        dto.setTopicId(request.getTopicId());
        dto.setPermissionCode(request.getPermissionCode());
        dto.setRequestReason(request.getRequestReason());
        dto.setStatus(request.getStatus());
        dto.setApprovalReason(request.getApprovalReason());
        return dto;
    }

    // ===================== DTO 转换方法 =====================

    private UserPermissionDTO convertToUserPermissionDTO(UserPermission permission) {
        UserPermissionDTO dto = new UserPermissionDTO();
        dto.setPermissionId(permission.getId());
        dto.setUserId(permission.getUserId());
        dto.setUserName(permission.getUserName());
        dto.setTopicId(permission.getTopicId());
        dto.setPermissionCode(permission.getPermissionCode());
        dto.setStatus(permission.getStatus());
        dto.setExpireTime(permission.getExpireTime());
        dto.setGrantedTime(permission.getGrantedTime());
        dto.setExpired(permission.getExpired());
        return dto;
    }

    /**
     * 权限信息接口（用于 SecurityContext）
     */
    public interface UserInfo {
        BigInteger getId();

        String getUsername();
    }

    /**
     * 权限检查响应 DTO
     */
    public static class CheckPermissionResponseDTO {
        private BigInteger userId;
        private BigInteger topicId;
        private String permissionCode;
        private boolean hasPermission;

        public CheckPermissionResponseDTO() {
        }

        public CheckPermissionResponseDTO(BigInteger userId, BigInteger topicId, String permissionCode, boolean hasPermission) {
            this.userId = userId;
            this.topicId = topicId;
            this.permissionCode = permissionCode;
            this.hasPermission = hasPermission;
        }

        public BigInteger getUserId() {
            return userId;
        }

        public void setUserId(BigInteger userId) {
            this.userId = userId;
        }

        public BigInteger getTopicId() {
            return topicId;
        }

        public void setTopicId(BigInteger topicId) {
            this.topicId = topicId;
        }

        public String getPermissionCode() {
            return permissionCode;
        }

        public void setPermissionCode(String permissionCode) {
            this.permissionCode = permissionCode;
        }

        public boolean isHasPermission() {
            return hasPermission;
        }

        public void setHasPermission(boolean hasPermission) {
            this.hasPermission = hasPermission;
        }
    }
}
