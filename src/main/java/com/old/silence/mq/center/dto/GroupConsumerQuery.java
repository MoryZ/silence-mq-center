package com.old.silence.mq.center.dto;

/**
 * @author moryzang
 */
public class GroupConsumerQuery {
    private boolean skipSysGroup;
    private String address;

    public boolean isSkipSysGroup() {
        return skipSysGroup;
    }

    public void setSkipSysGroup(boolean skipSysGroup) {
        this.skipSysGroup = skipSysGroup;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
