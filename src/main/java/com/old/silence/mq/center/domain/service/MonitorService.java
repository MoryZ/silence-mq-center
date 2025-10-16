
package com.old.silence.mq.center.domain.service;

import com.old.silence.mq.center.domain.model.ConsumerMonitorConfig;

import java.util.Map;

public interface MonitorService {
    boolean createOrUpdateConsumerMonitor(String name, ConsumerMonitorConfig config);

    Map<String, ConsumerMonitorConfig> queryConsumerMonitorConfig();

    ConsumerMonitorConfig queryConsumerMonitorConfigByGroupName(String consumeGroupName);

    boolean deleteConsumerMonitor(String consumeGroupName);
}
