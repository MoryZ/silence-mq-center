
package com.old.silence.mq.center.domain.service.impl;

import org.apache.rocketmq.common.MixAll;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Throwables;
import com.old.silence.mq.center.api.config.RMQConfigure;
import com.old.silence.mq.center.domain.model.ConsumerMonitorConfig;
import com.old.silence.mq.center.domain.service.MonitorService;
import com.old.silence.mq.center.util.JsonUtil;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MonitorServiceImpl implements MonitorService {


    private final RMQConfigure configure;

    private Map<String, ConsumerMonitorConfig> configMap = new ConcurrentHashMap<>();

    public MonitorServiceImpl(RMQConfigure configure) {
        this.configure = configure;
    }

    @Override
    public boolean createOrUpdateConsumerMonitor(String name, ConsumerMonitorConfig config) {
        configMap.put(name, config);// todo if write map success but write file fail
        writeToFile(getConsumerMonitorConfigDataPath(), configMap);
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
        writeToFile(getConsumerMonitorConfigDataPath(), configMap);
        return true;
    }

    //rocketmq.console.data.path/monitor/consumerMonitorConfig.json
    private String getConsumerMonitorConfigDataPath() {
        return configure.getRocketMqDashboardDataPath() + File.separatorChar + "monitor" + File.separatorChar + "consumerMonitorConfig.json";
    }

    private String getConsumerMonitorConfigDataPathBackUp() {
        return getConsumerMonitorConfigDataPath() + ".bak";
    }

    private void writeToFile(String path, Object data) {
        writeDataJsonToFile(path, JsonUtil.obj2String(data));
    }

    private void writeDataJsonToFile(String path, String dataStr) {
        try {
            MixAll.string2File(dataStr, path);
        }
        catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @PostConstruct
    private void loadData() {
        try {
            String content = MixAll.file2String(getConsumerMonitorConfigDataPath());
            if (content == null) {
                content = MixAll.file2String(getConsumerMonitorConfigDataPathBackUp());
            }
            if (content == null) {
                logger.info("No consumer monitor config file found, using empty map");
                return;
            }
            ConcurrentHashMap<String, ConsumerMonitorConfig> loadedMap = JsonUtil.string2Obj(content, new TypeReference<ConcurrentHashMap<String, ConsumerMonitorConfig>>() {
            });
            if (loadedMap != null) {
                configMap = loadedMap;
            }
        }
        catch (Exception e) {
            logger.error("Failed to load consumer monitor config from file, using empty map", e);
            configMap = new ConcurrentHashMap<>();
        }
    }
}
