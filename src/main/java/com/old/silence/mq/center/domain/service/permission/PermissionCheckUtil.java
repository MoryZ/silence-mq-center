package com.old.silence.mq.center.domain.service.permission;

import com.old.silence.mq.center.api.config.RMQConfigure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 权限检查工具类
 * 用于在Service层快速检查用户权限
 */
@Component
public class PermissionCheckUtil {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RMQConfigure rmqConfigure;

    /**
     * 获取当前登录用户ID
     * 从请求上下文获取（需要与认证系统集成）
     */
    public Long getCurrentUserId() {
        // TODO: 从SecurityContext或请求上下文获取当前用户ID
        // 这里是示例，实际需要根据项目的认证系统实现
        return null;
    }

    /**
     * 获取当前用户名
     */
    public String getCurrentUserName() {
        // TODO: 从SecurityContext或请求上下文获取当前用户名
        return null;
    }

    /**
     * 检查当前用户是否有权限（如果无权限抛出异常）
     */
    public void checkCurrentUserPermission(Long topicId, String permissionCode) {
        Long userId = getCurrentUserId();
        if (userId != null) {
            permissionService.checkPermission(userId, topicId, permissionCode);
        }
    }

    /**
     * 检查当前用户是否有权限（返回boolean）
     */
    public boolean currentUserHasPermission(Long topicId, String permissionCode) {
        Long userId = getCurrentUserId();
        if (userId != null) {
            return permissionService.hasPermission(userId, topicId, permissionCode);
        }
        return false;
    }

    /**
     * 检查是否已禁用权限控制（可用于开发/测试环境）
     */
    public boolean isPermissionCheckDisabled() {
        // 可以通过配置文件控制是否启用权限检查
        return false;
    }
}
