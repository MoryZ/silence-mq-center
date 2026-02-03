package com.old.silence.mq.center.domain.service.template;

import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.RPCHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consumer生命周期管理模板
 * 职责：
 * 1. 统一创建DefaultMQPullConsumer
 * 2. 处理ACL和TLS配置
 * 3. 提供try-with-resources模式支持
 * 4. 自动处理异常和资源清理
 */
public class ConsumerTemplate {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerTemplate.class);

    /**
     * 创建DefaultMQPullConsumer
     */
    public static DefaultMQPullConsumer createConsumer(RPCHook rpcHook, boolean useTLS) {
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer(MixAll.TOOLS_CONSUMER_GROUP, rpcHook);
        consumer.setUseTLS(useTLS);
        return consumer;
    }

    /**
     * 使用Consumer执行操作 - 自动处理生命周期
     * @param rpcHook ACL RPC钩子，如果不需要ACL则传null
     * @param useTLS 是否使用TLS
     * @param operation Consumer操作函数
     * @param <T> 返回值类型
     * @return 操作结果
     * @throws Exception 操作异常
     */
    public static <T> T executeWithConsumer(RPCHook rpcHook, boolean useTLS, ConsumerOperation<T> operation) throws Exception {
        DefaultMQPullConsumer consumer = createConsumer(rpcHook, useTLS);
        try {
            consumer.start();
            return operation.execute(consumer);
        } finally {
            consumer.shutdown();
        }
    }

    /**
     * 使用Consumer执行无返回值操作 - 自动处理生命周期
     */
    public static void executeWithConsumer(RPCHook rpcHook, boolean useTLS, ConsumerOperationVoid operation) throws Exception {
        DefaultMQPullConsumer consumer = createConsumer(rpcHook, useTLS);
        try {
            consumer.start();
            operation.execute(consumer);
        } finally {
            consumer.shutdown();
        }
    }

    /**
     * 创建ACL RPC钩子
     */
    public static RPCHook createAclHook(String accessKey, String secretKey) {
        if (accessKey != null && !accessKey.isEmpty() && secretKey != null && !secretKey.isEmpty()) {
            return new AclClientRPCHook(new SessionCredentials(accessKey, secretKey));
        }
        return null;
    }

    /**
     * Consumer操作接口 - 返回值
     */
    @FunctionalInterface
    public interface ConsumerOperation<T> {
        T execute(DefaultMQPullConsumer consumer) throws Exception;
    }

    /**
     * Consumer操作接口 - 无返回值
     */
    @FunctionalInterface
    public interface ConsumerOperationVoid {
        void execute(DefaultMQPullConsumer consumer) throws Exception;
    }
}
