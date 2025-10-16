
package com.old.silence.mq.center.domain.model;

import org.apache.rocketmq.remoting.protocol.admin.RollbackStats;

import java.util.ArrayList;
import java.util.List;

public class ConsumerGroupRollBackStat {
    private boolean status;
    private String errMsg;
    private List<RollbackStats> rollbackStatsList = new ArrayList<>();

    public ConsumerGroupRollBackStat(boolean status) {
        this.status = status;
    }

    public ConsumerGroupRollBackStat(boolean status, String errMsg) {
        this.status = status;
        this.errMsg = errMsg;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<RollbackStats> getRollbackStatsList() {
        return rollbackStatsList;
    }

    public void setRollbackStatsList(List<RollbackStats> rollbackStatsList) {
        this.rollbackStatsList = rollbackStatsList;
    }
}
