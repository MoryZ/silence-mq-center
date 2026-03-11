package com.old.silence.mq.center.domain.service.impl;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.old.silence.mq.center.api.config.RMQConfigure;
import com.old.silence.mq.center.domain.model.ConsumerMonitorConfig;
import com.old.silence.mq.center.domain.service.MonitorService;
import com.old.silence.mq.center.domain.service.facade.RocketMQClientFacade;
import com.old.silence.mq.center.domain.service.helper.MonitorConfigHelper;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MonitorServiceImpl implements MonitorService {


    private final RMQConfigure configure;
    private final RocketMQClientFacade mqFacade;
    private Map<String, ConsumerMonitorConfig> configMap = new ConcurrentHashMap<>();

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

        File primaryFile = new File(primaryPath);
        File backupFile = new File(backupPath);

        // 首次启动时主备文件都不存在，初始化空配置文件，避免无意义告警。
        if (!primaryFile.exists() && !backupFile.exists()) {
            File parent = primaryFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            MonitorConfigHelper.writeToFile(primaryPath, configMap);
            return;
        }

        ConcurrentHashMap<String, ConsumerMonitorConfig> loadedMap =
                MonitorConfigHelper.loadFromFile(
                        primaryPath,
                        backupPath,
                        new TypeReference<ConcurrentHashMap<String, ConsumerMonitorConfig>>() {
                        });

        if (loadedMap != null) {
            configMap = loadedMap;
        }
    }
}
