package com.old.silence.mq.center.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.old.silence.mq.center.domain.service.UserPermissionService;
import com.old.silence.mq.center.dto.GrantPermissionCommand;
import com.old.silence.mq.center.dto.RevokePermissionCommand;

import java.math.BigInteger;

/**
 *
 * @author moryzang
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1")
public class UserPermissionController {

    private static final Logger log = LoggerFactory.getLogger(UserPermissionController.class);

    private final UserPermissionService userPermissionService;

    public UserPermissionController(UserPermissionService userPermissionService) {
        this.userPermissionService = userPermissionService;
    }


    /**
     * 直接赋予权限（管理员操作）
     * <p>
     * 管理员直接为用户赋予权限，无需审批流程
     *
     * @param command 赋予权限请求
     */
    @PostMapping("/userPermissions/grant")
    public void grantPermission(@RequestBody GrantPermissionCommand command) {
        try {
            log.info("管理员 为用户 {} 赋予权限: permissionCode={}", command.getUserName(), command.getPermissionCode());
            userPermissionService.grantPermission(command.getUserName(), command.getPermissionCode(), command.getExpireTime());
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
     * @param command 撤销权限请求
     */
    @PutMapping("/userPermissions/revoke")
    public void revokePermission(@RequestBody RevokePermissionCommand command) {
        try {
            log.info("管理员撤销用户 {} 的 {} 权限", command.getUserName(), command.getPermissionCode());

            userPermissionService.revokePermission(command.getUserName(), command.getPermissionCode());
        } catch (Exception e) {
            log.error("撤销权限失败", e);
            throw e;
        }
    }


}
