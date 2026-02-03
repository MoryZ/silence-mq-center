package com.old.silence.mq.center.domain.service.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Throwables;
import com.old.silence.mq.center.util.JsonUtil;
import org.apache.rocketmq.common.MixAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * 监控配置文件管理辅助类
 * 职责：处理监控配置的文件读写操作
 */
public class MonitorConfigHelper {

    private static final Logger logger = LoggerFactory.getLogger(MonitorConfigHelper.class);

    /**
     * 从文件加载配置
     * @param primaryPath 主文件路径
     * @param backupPath 备份文件路径
     * @param typeRef 类型引用
     * @return 加载的配置对象
     */
    public static <T> T loadFromFile(String primaryPath, String backupPath, TypeReference<T> typeRef) throws IOException {
        String content = MixAll.file2String(primaryPath);
        
        if (content == null) {
            logger.info("Primary config file not found, trying backup: {}", backupPath);
            content = MixAll.file2String(backupPath);
        }

        if (content == null) {
            logger.warn("Neither primary nor backup config file found");
            return null;
        }

        try {
            return JsonUtil.string2Obj(content, typeRef);
        } catch (Exception e) {
            logger.error("Failed to parse config from file: {}", primaryPath, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 将配置写入文件
     * @param path 文件路径
     * @param data 配置数据
     */
    public static void writeToFile(String path, Object data) {
        String dataStr = JsonUtil.obj2String(data);
        writeDataToFile(path, dataStr);
    }

    /**
     * 将JSON字符串写入文件
     * @param path 文件路径
     * @param dataStr JSON字符串
     */
    public static void writeDataToFile(String path, String dataStr) {
        try {
            MixAll.string2File(dataStr, path);
            logger.debug("Config written to file: {}", path);
        } catch (Exception e) {
            logger.error("Failed to write config to file: {}", path, e);
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 构建配置文件路径
     */
    public static String buildConfigPath(String basePath, String... pathComponents) {
        StringBuilder sb = new StringBuilder(basePath);
        for (String component : pathComponents) {
            sb.append(File.separatorChar).append(component);
        }
        return sb.toString();
    }

    /**
     * 构建备份文件路径
     */
    public static String buildBackupPath(String primaryPath) {
        return primaryPath + ".bak";
    }
}
