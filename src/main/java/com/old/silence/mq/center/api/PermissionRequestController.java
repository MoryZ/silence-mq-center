package com.old.silence.mq.center.api;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.old.silence.mq.center.api.util.PermissionContextHolder;
import com.old.silence.mq.center.api.util.PermissionDTOConverter;
import com.old.silence.mq.center.domain.service.PermissionService;
import com.old.silence.mq.center.dto.PermissionRequestDTO;
import com.old.silence.mq.center.vo.PermissionRequestResponseDTO;

/**
 * 权限申请管理 REST API 控制器
 * <p>
 * 提供权限申请和申请记录查询功能的 HTTP 端点
 * <p>
 * API 端点：
 * - POST /api/permission-requests/request - 用户申请权限
 * - GET /api/permission-requests/pending - 获取待审批权限申请列表
 * - GET /api/permission-requests/user/{userId} - 获取用户的权限申请记录
 *
 * @author Silence
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/permission-requests")
public class PermissionRequestController {

    private static final Logger log = LoggerFactory.getLogger(PermissionRequestController.class);

    private final PermissionService permissionService;
    private final PermissionDTOConverter dtoConverter;

    public PermissionRequestController(
            PermissionService permissionService,
            PermissionDTOConverter dtoConverter) {
        this.permissionService = permissionService;
        this.dtoConverter = dtoConverter;
    }

    /**
     * 申请权限
     * <p>
     * 用户申请对某个 Topic 的特定权限，申请会进入待审批状态
     * 需要管理员审批通过才能获得权限
     *
     * @param request 权限申请请求
     * @return 申请的权限请求ID
     */
    @PostMapping("/request")
    public BigInteger requestPermission(@RequestBody PermissionRequestDTO request) {
        try {
            BigInteger userId = PermissionContextHolder.getCurrentUserId();
            String userName = PermissionContextHolder.getCurrentUserName();

            log.info("用户 {} 申请权限: topicId={}, permissionCode={}, reason={}",
                    userId, request.getTopicId(), request.getPermissionCode(), request.getReason());

            // 调用权限服务申请权限
            return permissionService.requestPermission(
                    userId,
                    userName,
                    request.getTopicId(),
                    request.getPermissionCode(),
                    request.getReason()
            );

        } catch (Exception e) {
            log.error("权限申请失败", e);
            throw e;
        }
    }

    /**
     * 获取待审批的权限申请列表
     * <p>
     * 管理员查询所有待审批的权限申请
     *
     * @param page 页码（默认 0）
     * @param size 每页数量（默认 20）
     * @return 待审批申请列表
     */
    @GetMapping("/pending")
    public List<PermissionRequestResponseDTO> getPendingRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.debug("获取待审批权限申请列表，page={}, size={}", page, size);

            return permissionService.getPendingRequests()
                    .stream()
                    .skip((long) page * size)
                    .limit(size)
                    .map(dtoConverter::toRequestResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取待审批申请失败", e);
            throw e;
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
    @GetMapping("/user/{userId}")
    public List<PermissionRequestResponseDTO> getUserRequests(
            @PathVariable BigInteger userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.debug("获取用户 {} 的权限申请记录", userId);

            return permissionService.getUserRequests(userId)
                    .stream()
                    .skip((long) page * size)
                    .limit(size)
                    .map(dtoConverter::toRequestResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取申请记录失败", e);
            throw e;
        }
    }
}
