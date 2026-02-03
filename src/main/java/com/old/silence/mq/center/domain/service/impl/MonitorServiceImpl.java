
package com.old.silence.mq.center.domain.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.old.silence.mq.center.api.config.RMQConfigure;
import com.old.silence.mq.center.domain.model.ConsumerMonitorConfig;
import com.old.silence.mq.center.domain.service.MonitorService;
import com.old.silence.mq.center.domain.service.facade.RocketMQClientFacade;
import com.old.silence.mq.center.domain.service.helper.MonitorConfigHelper;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MonitorServiceImpl implements MonitorService {


    private final RMQConfigure configure;

    private Map<String, ConsumerMonitorConfig> configMap = new ConcurrentHashMap<>();
    
    private final RocketMQClientFacade mqFacade;

    public MonitorServiceImpl(RMQConfigure configure, RocketMQClientFacade mqFacade) {
        this.configure = configure;
        this.mqFacade = mqFacade;
    }

    @Override
    public boolean createOrUpdateConsumerMonitor(String name, ConsumerMonitorConfig config) {
        configMap.put(name, config);
        MonitorConfigHelper.writeToFile(getConsumerMonitorConfigDataPath(), configMap);
        return true;
    }

    @Override
    public Map<String, ConsumerMonitorConfig> queryConsumerMonitorConfig() {
        return configMap;
    }

    @Override
    public ConsumerMonitorConfig queryConsumerMonitorConfigByGroupName(String consumeGroupName) {
        return configMap.get(consumeGroupName);
    }

    @Override
    public boolean deleteConsumerMonitor(String consumeGroupName) {
        configMap.remove(consumeGroupName);
        MonitorConfigHelper.writeToFile(getConsumerMonitorConfigDataPath(), configMap);
        return true;
    }

    //rocketmq.console.data.path/monitor/consumerMonitorConfig.json
    private String getConsumerMonitorConfigDataPath() {
        return MonitorConfigHelper.buildConfigPath(
                configure.getRocketMqDashboardDataPath(),
                "monitor",
                "consumerMonitorConfig.json");
    }

    private String getConsumerMonitorConfigDataPathBackUp() {
        return MonitorConfigHelper.buildBackupPath(getConsumerMonitorConfigDataPath());
    }

    @PostConstruct
    private void loadData() throws IOException {
        String primaryPath = getConsumerMonitorConfigDataPath();
        String backupPath = getConsumerMonitorConfigDataPathBackUp();
        
        ConcurrentHashMap<String, ConsumerMonitorConfig> loadedMap = 
            MonitorConfigHelper.loadFromFile(
                primaryPath,
                backupPath,
                new TypeReference<ConcurrentHashMap<String, ConsumerMonitorConfig>>() {});
        
        if (loadedMap != null) {
            configMap = loadedMap;
        }
    }
}
