package com.old.silence.mq.center.domain.service.helper;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.AclConfig;
import org.apache.rocketmq.common.PlainAccessConfig;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Throwables;
import com.old.silence.mq.center.domain.service.facade.RocketMQClientFacade;

import java.util.List;
import java.util.Optional;

/**
 * ACL配置辅助类
 * 职责：处理ACL配置的查询、更新和同步操作
 */
public class AclConfigHelper {

    private static final Logger logger = LoggerFactory.getLogger(AclConfigHelper.class);

    /**
     * 在所有Broker上执行ACL操作
     *
     * @param brokerAddrs Broker地址集合
     * @param operation   操作函数
     */
    public static void executeBrokerOperation(Iterable<String> brokerAddrs, BrokerOperation operation) {
        try {
            for (String addr : brokerAddrs) {
                operation.execute(addr);
            }
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 在所有Broker上查询并执行ACL操作
     *
     * @param brokerAddrs Broker地址集合
     * @param mqAdminExt  MQAdmin实例
     * @param operation   需要远程配置的操作
     */
    public static void executeAclConfigOperation(Iterable<String> brokerAddrs, MQAdminExt mqAdminExt,
                                                 AclConfigOperation operation) {
        try {
            for (String addr : brokerAddrs) {
                AclConfig aclConfig = mqAdminExt.examineBrokerClusterAclConfig(addr);
                operation.execute(addr, aclConfig);
            }
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 在所有Broker上查询并执行ACL操作（使用Facade）
     */
    public static void executeAclConfigOperation(Iterable<String> brokerAddrs, RocketMQClientFacade mqFacade,
                                                 AclConfigOperation operation) {
        try {
            for (String addr : brokerAddrs) {
                AclConfig aclConfig = mqFacade.getAclConfig(addr);
                operation.execute(addr, aclConfig);
            }
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询某个AccessKey是否存在
     */
    public static boolean isExistAccessKey(MQAdminExt mqAdminExt, String accessKey, String brokerAddr) {
        try {
            AclConfig aclConfig = mqAdminExt.examineBrokerClusterAclConfig(brokerAddr);
            List<PlainAccessConfig> configs = aclConfig.getPlainAccessConfigs();

            if (configs == null || configs.isEmpty()) {
                return false;
            }

            return configs.stream()
                    .anyMatch(config -> accessKey.equals(config.getAccessKey()));
        } catch (Exception e) {
            logger.error("Failed to check access key existence: {}", accessKey, e);
            return false;
        }
    }

    /**
     * 查询某个AccessKey是否存在（使用Facade）
     */
    public static boolean isExistAccessKey(RocketMQClientFacade mqFacade, String accessKey, String brokerAddr) {
        try {
            AclConfig aclConfig = mqFacade.getAclConfig(brokerAddr);
            List<PlainAccessConfig> configs = aclConfig.getPlainAccessConfigs();

            if (configs == null || configs.isEmpty()) {
                return false;
            }

            return configs.stream()
                    .anyMatch(config -> accessKey.equals(config.getAccessKey()));
        } catch (Exception e) {
            logger.error("Failed to check access key existence: {}", accessKey, e);
            return false;
        }
    }

    /**
     * 查找指定AccessKey的配置
     */
    public static Optional<PlainAccessConfig> findAccessKeyConfig(AclConfig aclConfig, String accessKey) {
        List<PlainAccessConfig> configs = aclConfig.getPlainAccessConfigs();

        if (configs == null || configs.isEmpty()) {
            return Optional.empty();
        }

        return configs.stream()
                .filter(config -> accessKey.equals(config.getAccessKey()))
                .findFirst();
    }

    /**
     * 从列表中删除指定权限配置
     * 按照"name=perm"的格式查找和删除
     */
    public static void removePermByName(List<String> permList, String name) {
        if (permList == null || permList.isEmpty()) {
            return;
        }

        permList.removeIf(perm -> {
            String permName = perm.split("=")[0];
            return permName.equals(name);
        });
    }

    /**
     * 从字符串中提取权限名称
     * 从"name=perm"格式的字符串中提取name部分
     */
    public static String extractPermName(String permString) {
        if (StringUtils.isEmpty(permString)) {
            return null;
        }

        String[] parts = permString.split("=");
        return parts.length > 0 ? parts[0] : null;
    }

    /**
     * Broker操作接口
     */
    @FunctionalInterface
    public interface BrokerOperation {
        void execute(String brokerAddr) throws Exception;
    }

    /**
     * ACL配置操作接口
     */
    @FunctionalInterface
    public interface AclConfigOperation {
        void execute(String brokerAddr, AclConfig aclConfig) throws Exception;
    }
}
