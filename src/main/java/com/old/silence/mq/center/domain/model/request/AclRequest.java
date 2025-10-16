
package com.old.silence.mq.center.domain.model.request;

import org.apache.rocketmq.common.PlainAccessConfig;

public class AclRequest {

    private PlainAccessConfig config;

    private String topicPerm;

    private String groupPerm;

    public PlainAccessConfig getConfig() {
        return config;
    }

    public void setConfig(PlainAccessConfig config) {
        this.config = config;
    }

    public String getTopicPerm() {
        return topicPerm;
    }

    public void setTopicPerm(String topicPerm) {
        this.topicPerm = topicPerm;
    }

    public String getGroupPerm() {
        return groupPerm;
    }

    public void setGroupPerm(String groupPerm) {
        this.groupPerm = groupPerm;
    }
}
