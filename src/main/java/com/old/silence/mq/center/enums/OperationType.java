package com.old.silence.mq.center.enums;

import com.old.silence.core.enums.DescribedEnumValue;

/**
 * @author moryzang
 */
public enum OperationType implements DescribedEnumValue<String> {

    REQUEST("REQUEST", "申请权限"),
    APPROVE("APPROVE", "批准申请"),
    REJECT("REJECT", "拒绝申请"),
    GRANT("GRANT", "授予权限"),
    REVOKE("REVOKE", "撤销权限"),
    EXPIRE("EXPIRE", "撤销权限"),
    ;

    private final String value;

    private final String description;

    OperationType(String value, String description) {
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
