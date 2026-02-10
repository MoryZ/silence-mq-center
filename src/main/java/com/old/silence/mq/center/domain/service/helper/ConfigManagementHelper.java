package com.old.silence.mq.center.domain.service.helper;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * 配置管理辅助类
 * 职责：处理配置更新和池清理操作
 */
public class ConfigManagementHelper {

    private static final Logger logger = LoggerFactory.getLogger(ConfigManagementHelper.class);

    /**
     * 更新配置并清理对象池
     *
     * @param updateOperation 配置更新操作
     * @param poolToClear     需要清理的对象池（可选）
     */
    public static void updateConfigAndClearPool(Runnable updateOperation, GenericObjectPool<?> poolToClear) {
        try {
            updateOperation.run();
            if (poolToClear != null) {
                poolToClear.clear();
                logger.debug("Object pool cleared after config update");
            }
        } catch (Exception e) {
            logger.error("Failed to update config and clear pool", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 构建信息Map
     */
    public static <K, V> Map<K, V> buildInfoMap() {
        return Maps.newHashMap();
    }

    /**
     * 添加配置项到Map
     */
    public static <K, V> Map<K, V> putConfigToMap(Map<K, V> map, K key, V value) {
        map.put(key, value);
        return map;
    }
}
