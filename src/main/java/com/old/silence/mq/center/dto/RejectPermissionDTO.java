package com.old.silence.mq.center.dto;

import java.math.BigInteger;

/**
 * @author moryzang
 */
public class RejectPermissionDTO {

    private BigInteger requestId;

    private String rejectionReason;

    public BigInteger getRequestId() {
        return requestId;
    }

    public void setRequestId(BigInteger requestId) {
        this.requestId = requestId;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
