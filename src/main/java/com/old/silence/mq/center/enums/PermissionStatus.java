package com.old.silence.mq.center.enums;

import com.old.silence.core.enums.DescribedEnumValue;

/**
 * @author moryzang
 */
public enum PermissionStatus implements DescribedEnumValue<String> {
    PENDING("PENDING", "待审批"),
    APPROVED("APPROVED", "已批准"),
    REJECTED("REJECTED", "已拒绝"),
    EXPIRED("EXPIRED", "已过期"),
    WITHDRAWN("WITHDRAWN", "已撤回"),
    ;

    private final String value;

    private final String description;

    PermissionStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getDescription() {
        return description;
    }

}
