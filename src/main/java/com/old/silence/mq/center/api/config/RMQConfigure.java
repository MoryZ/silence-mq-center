package com.old.silence.mq.center.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * RocketMQ 配置管理
 *
 * @author moryzang
 */
@Component
public class RMQConfigure {

    @Value("${rocketmq.dashboard.datapath:./data}")
    private String dataPath;

    @Value("${rocketmq.acl.enable:false}")
    private Boolean aclEnabled;

    @Value("${rocketmq.namesrv.addr:192.168.50.162:9876}")
    private String namesrvAddr;

    @Value("${rocketmq.acl.access-key:}")
    private String accessKey;

    @Value("${rocketmq.acl.secret-key:}")
    private String secretKey;

    @Value("${rocketmq.tls.enable:false}")
    private boolean useTLS;

    public String getRocketMqDashboardDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public Boolean isACLEnabled() {
        return aclEnabled != null && aclEnabled;
    }

    public void setACLEnabled(Boolean aclEnabled) {
        this.aclEnabled = aclEnabled;
    }

    public String getNamesrvAddr() {
        return namesrvAddr;
    }

    public void setNamesrvAddr(String namesrvAddr) {
        this.namesrvAddr = namesrvAddr;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public boolean isUseTLS() {
        return useTLS;
    }

    public String getDashboardCollectData() {
        return dataPath + File.separator + "dashboard";
    }
}
