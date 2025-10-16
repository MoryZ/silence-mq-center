

package com.old.silence.mq.center.domain.model.trace;


public enum MessageTraceStatusEnum {
    SUCCESS("success"),
    FAILED("failed"),
    UNKNOWN("unknown");
    private final String status;

    MessageTraceStatusEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
