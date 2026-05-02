package com.old.silence.mq.center.enums;

import com.old.silence.core.enums.DescribedEnumValue;

/**
 * @author moryzang
 */
public enum TopicStatus implements DescribedEnumValue<String> {

    ACTIVE("ACTIVE", "激活状态"),
    INACTIVE("INACTIVE", "禁用"),
    DELETED("DELETED", "激活状态"),
    ;

    private final String value;

    private final String description;

    TopicStatus(String value, String description) {
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
