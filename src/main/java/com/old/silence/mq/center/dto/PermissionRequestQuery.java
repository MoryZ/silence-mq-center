package com.old.silence.mq.center.dto;

import com.old.silence.data.commons.annotation.RelationalQueryProperty;
import com.old.silence.data.commons.converter.Part;
import com.old.silence.mq.center.enums.PermissionStatus;

/**
 * @author moryzang
 */
public class PermissionRequestQuery {

    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private PermissionStatus permissionStatus;

    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private String permissionCode;


    public PermissionStatus getPermissionStatus() {
        return permissionStatus;
    }

    public void setPermissionStatus(PermissionStatus permissionStatus) {
        this.permissionStatus = permissionStatus;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }
}
