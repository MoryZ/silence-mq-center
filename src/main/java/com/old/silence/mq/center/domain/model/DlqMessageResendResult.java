package com.old.silence.mq.center.domain.model;

import org.apache.rocketmq.remoting.protocol.body.CMResult;
import org.apache.rocketmq.remoting.protocol.body.ConsumeMessageDirectlyResult;

public class DlqMessageResendResult {
    private CMResult consumeResult;
    private String remark;
    private String msgId;

    public DlqMessageResendResult(ConsumeMessageDirectlyResult consumeMessageDirectlyResult, String msgId) {
        this.consumeResult = consumeMessageDirectlyResult.getConsumeResult();
        this.remark = consumeMessageDirectlyResult.getRemark();
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
