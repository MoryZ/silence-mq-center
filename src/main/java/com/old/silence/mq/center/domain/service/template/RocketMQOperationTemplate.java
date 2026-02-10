package com.old.silence.mq.center.domain.service.template;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.google.common.base.Throwables;
import com.old.silence.mq.center.domain.service.pool.MQClientConnectionPool;
import com.old.silence.mq.center.exception.ServiceException;

/**
 * RocketMQ 操作执行模板
 * <p>
 * 目的：
 * 1. 消除重复的异常处理代码
 * 2. 统一资源生命周期管理（借用/归还连接）
 * 3. 统一的日志记录
 * 4. 支持自定义的前置/后置处理
 * <p>
 * 原理：模板方法模式 + 泛型
 * <p>
 * 使用示例：
 * <pre>
 * List<String> topics = template.executeProducerOp(
 *     producer -> {
 *         return getTopicsFromProducer(producer);
 *     },
 *     "getTopics"
 * );
 * </pre>
 */
@Component
public class RocketMQOperationTemplate {

    private static final Logger logger = LoggerFactory.getLogger(
            RocketMQOperationTemplate.class);

    private final MQClientConnectionPool connectionPool;
    private final MQAdminExt mqAdminExt;

    public RocketMQOperationTemplate(
            MQClientConnectionPool connectionPool,
            MQAdminExt mqAdminExt) {
        this.connectionPool = connectionPool;
        this.mqAdminExt = mqAdminExt;
    }

    /**
     * 执行 Producer 操作（返回结果）
     * <p>
     * 流程：
     * 1. 从连接池借用 Producer
     * 2. 执行操作
     * 3. 处理异常
     * 4. 归还 Producer（或失效）
     *
     * @param operation     操作函数
     * @param operationName 操作名称（用于日志）
     * @return 操作结果
     * @throws ServiceException 包装后的异常
     */
    public <T> T executeProducerOp(
            ProducerOperation<T> operation,
            String operationName) {

        DefaultMQProducer producer = null;
        try {
            // 步骤1：从池中借用
            producer = connectionPool.borrowProducer();
            logger.debug("op=start operationName={}, producer={}",
                    operationName, producer.getProducerGroup());

            // 步骤2：执行操作
            T result = operation.execute(producer);

            logger.debug("op=success operationName={}", operationName);
            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException(-1,
                    operationName + " was interrupted");

        } catch (Exception e) {
            logger.error("op=fail operationName={}, error={}",
                    operationName, e.getMessage(), e);

            // 尝试从池中失效这个 Producer（它可能不健康）
            if (producer != null) {
                try {
                    connectionPool.invalidateProducer(producer);
                    logger.warn("Invalidated unhealthy producer");
                    producer = null;  // 防止下面的 finally 重复归还
                } catch (Exception ex) {
                    logger.warn("Failed to invalidate producer", ex);
                }
            }

            // 转换异常
            Throwables.throwIfUnchecked(e);
            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            }
            throw new ServiceException(-1,
                    operationName + " failed: " + e.getMessage());

        } finally {
            // 步骤4：归还 Producer
            if (producer != null) {
                try {
                    connectionPool.returnProducer(producer);
                } catch (Exception e) {
                    logger.warn("Failed to return producer to pool", e);
                }
            }
        }
    }

    /**
     * 执行 Producer 操作（无返回值）
     * <p>
     * 这是 executeProducerOp 的便利方法
     */
    public void executeProducerOpVoid(
            ProducerOperationVoid operation,
            String operationName) {

        executeProducerOp(producer -> {
            operation.execute(producer);
            return null;
        }, operationName);
    }

    /**
     * 执行 AdminExt 操作
     * <p>
     * AdminExt 是单例，无需池化，但仍需统一的异常处理
     *
     * @param operation     操作函数
     * @param operationName 操作名称
     * @return 操作结果
     */
    public <T> T executeAdminOp(
            AdminOperation<T> operation,
            String operationName) {

        try {
            logger.debug("op=start operationName={}", operationName);
            T result = operation.execute(mqAdminExt);
            logger.debug("op=success operationName={}", operationName);
            return result;

        } catch (Exception e) {
            logger.error("op=fail operationName={}, error={}",
                    operationName, e.getMessage(), e);

            Throwables.throwIfUnchecked(e);
            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            }
            throw new ServiceException(-1,
                    operationName + " failed: " + e.getMessage());
        }
    }

    /**
     * 执行 AdminExt 操作（无返回值）
     */
    public void executeAdminOpVoid(
            AdminOperationVoid operation,
            String operationName) {

        executeAdminOp(admin -> {
            operation.execute(admin);
            return null;
        }, operationName);
    }

    /**
     * 获取连接池状态（用于监控）
     */
    public MQClientConnectionPool.PoolStats getPoolStats() {
        return connectionPool.getPoolStats();
    }

    // ============ Functional Interfaces ============

    /**
     * Producer 操作接口（有返回值）
     */
    @FunctionalInterface
    public interface ProducerOperation<T> {
        /**
         * 执行操作
         *
         * @param producer 可用的 Producer 实例
         * @return 操作结果
         * @throws Exception 任何异常都会被捕获和包装
         */
        T execute(DefaultMQProducer producer) throws Exception;
    }

    /**
     * Producer 操作接口（无返回值）
     */
    @FunctionalInterface
    public interface ProducerOperationVoid {
        void execute(DefaultMQProducer producer) throws Exception;
    }

    /**
     * AdminExt 操作接口（有返回值）
     */
    @FunctionalInterface
    public interface AdminOperation<T> {
        /**
         * 执行操作
         *
         * @param admin 可用的 MQAdminExt 实例
         * @return 操作结果
         * @throws Exception 任何异常都会被捕获和包装
         */
        T execute(MQAdminExt admin) throws Exception;
    }

    /**
     * AdminExt 操作接口（无返回值）
     */
    @FunctionalInterface
    public interface AdminOperationVoid {
        void execute(MQAdminExt admin) throws Exception;
    }
}
