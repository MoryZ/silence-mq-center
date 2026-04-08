package com.old.silence.mq.center.api;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.old.silence.mq.center.api.util.PermissionContextHolder;
import com.old.silence.mq.center.api.util.PermissionDTOConverter;
import com.old.silence.mq.center.domain.service.PermissionService;
import com.old.silence.mq.center.dto.UserPermissionDTO;
import com.old.silence.mq.center.vo.CheckPermissionResponseVO;

/**
 * 权限查询 REST API 控制器
 * <p>
 * 提供权限查询和权限检查功能的 HTTP 端点
 * <p>
 * API 端点：
 * - GET /api/permissions/my-permissions - 获取当前用户的权限列表
 * - GET /api/permissions/user/{userId} - 获取指定用户的权限列表
 * - GET /api/permissions/topic/{topicId} - 获取 Topic 的权限列表
 * - GET /api/permissions/user/{userId}/topic/{topicId} - 获取用户在 Topic 上的权限列表
 * - GET /api/permissions/check - 检查用户是否有指定权限
 *
 * @author Silence
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/permissions")
public class PermissionQueryController {

    private static final Logger log = LoggerFactory.getLogger(PermissionQueryController.class);

    private final PermissionService permissionService;
    private final PermissionDTOConverter dtoConverter;

    public PermissionQueryController(
            PermissionService permissionService,
            PermissionDTOConverter dtoConverter) {
        this.permissionService = permissionService;
        this.dtoConverter = dtoConverter;
    }

    /**
     * 获取当前用户的权限列表
     * <p>
     * 返回当前认证用户在所有 Topic 上的有效权限
     *
     * @return 用户权限列表
     */
    @GetMapping("/my-permissions")
    public List<UserPermissionDTO> getMyPermissions() {
        try {
            BigInteger userId = PermissionContextHolder.getCurrentUserId();
            log.debug("获取用户 {} 的权限列表", userId);

            return permissionService.getUserPermissions(userId)
                    .stream()
                    .map(dtoConverter::toUserPermissionDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取权限列表失败", e);
            throw e;
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
    public List<UserPermissionDTO> getUserPermissions(@PathVariable BigInteger userId) {
        try {
            log.debug("获取用户 {} 的权限列表", userId);

            return permissionService.getUserPermissions(userId)
                    .stream()
                    .map(dtoConverter::toUserPermissionDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取权限列表失败", e);
            throw e;
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
    public List<UserPermissionDTO> getTopicPermissions(@PathVariable BigInteger topicId) {
        try {
            log.debug("获取 Topic {} 的权限列表", topicId);

            return permissionService.getTopicPermissions(topicId)
                    .stream()
                    .map(dtoConverter::toUserPermissionDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取权限列表失败", e);
            throw e;
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
    public List<UserPermissionDTO> getUserTopicPermissions(
            @PathVariable BigInteger userId,
            @PathVariable BigInteger topicId) {
        try {
            log.debug("获取用户 {} 在 Topic {} 上的权限", userId, topicId);

            return permissionService.getUserPermissionsByTopic(userId, topicId)
                    .stream()
                    .map(dtoConverter::toUserPermissionDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取权限列表失败", e);
            throw e;
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
    public CheckPermissionResponseVO checkPermission(
            @RequestParam("userId") BigInteger userId,
            @RequestParam(value = "topicId", required = false) BigInteger topicId,
            @RequestParam("permissionCode") String permissionCode) {
        try {
            log.debug("检查用户 {} 是否有权限 {} 在 Topic {}", userId, permissionCode, topicId);

            boolean hasPermission = permissionService.hasPermission(userId, topicId, permissionCode);

            return new CheckPermissionResponseVO(userId, topicId, permissionCode, hasPermission);
        } catch (Exception e) {
            log.error("权限检查失败", e);
            throw e;
        }
    }
}
