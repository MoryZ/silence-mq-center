package com.old.silence.mq.center.dto;

/**
 * 权限赋予请求DTO
 * <p>
 * 用于管理员直接为用户赋予权限
 *
 * @author Silence
 * @since 2024-01-01
 */
public class RevokePermissionCommand {

    /**
     * 目标用户名
     */
    private String userName;

    /**
     * 权限代码（如PRODUCE, CONSUME等）
     */
    private String permissionCode;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

}
