package com.old.silence.mq.center.enums;

import com.old.silence.core.enums.DescribedEnumValue;

/**
 * @author moryzang
 */
public enum MessageType implements DescribedEnumValue<String> {
    DELAY("DELAY", "延迟消息"),
    TRANSACTION("TRANSACTION", "事务消息"),
    FIFO("FIFO", "FIFO消息"),
    NORMAL("NORMAL", "普通消息"),
    ;

    private final String value;

    private final String description;

    MessageType(String value, String description) {
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
