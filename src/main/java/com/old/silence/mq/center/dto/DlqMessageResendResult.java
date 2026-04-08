package com.old.silence.mq.center.dto;

import org.apache.rocketmq.remoting.protocol.body.CMResult;
import org.apache.rocketmq.remoting.protocol.body.ConsumeMessageDirectlyResult;

public class DlqMessageResendResult {
    private CMResult consumeResult;
    private String remark;
    private String msgId;

    public DlqMessageResendResult() {
    }

    public DlqMessageResendResult(String msgId, String remark) {
        this.consumeResult = null;
        this.remark = remark;
        this.msgId = msgId;
    }

    public DlqMessageResendResult(ConsumeMessageDirectlyResult consumeMessageDirectlyResult, String msgId) {
        this.consumeResult = consumeMessageDirectlyResult != null ? consumeMessageDirectlyResult.getConsumeResult() : null;
        this.remark = consumeMessageDirectlyResult != null ? consumeMessageDirectlyResult.getRemark() : null;
        this.msgId = msgId;
    }

    public CMResult getConsumeResult() {
        return consumeResult;
    }

    public void setConsumeResult(CMResult consumeResult) {
        this.consumeResult = consumeResult;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
}
