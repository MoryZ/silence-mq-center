package com.old.silence.mq.center.domain.service.pool;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.topic.TopicValidator;
import org.apache.rocketmq.remoting.RPCHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import com.google.common.base.Throwables;
import com.old.silence.mq.center.api.config.RMQConfigure;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * RocketMQ 客户端连接池管理器
 * <p>
 * 核心职责：
 * 1. 管理 Producer 连接池（复用 DefaultMQProducer）
 * 2. 管理事务 Producer（特殊处理）
 * 3. 提供生命周期管理
 * <p>
 * 性能收益：
 * - 避免频繁创建销毁连接
 * - 复用内部的 NettyClientConfig
 * - 大幅降低 CPU 和内存占用
 * <p>
 * 使用示例：
 * <pre>
 * DefaultMQProducer producer = connectionPool.borrowProducer();
 * try {
 *     SendResult result = producer.send(message);
 * } finally {
 *     connectionPool.returnProducer(producer);
 * }
 * </pre>
 */
@Component
public class MQClientConnectionPool implements InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(MQClientConnectionPool.class);

    private final RMQConfigure configure;
    // 事务 Producer 队列（不使用池，因为事务需要单独配置Listener）
    private final BlockingQueue<TransactionMQProducer> transactionProducerQueue =
            new LinkedBlockingQueue<>(10);
    // Producer 连接池
    private GenericObjectPool<DefaultMQProducer> producerPool;
    // 池状态标记
    private volatile boolean initialized = false;

    public MQClientConnectionPool(RMQConfigure configure) {
        this.configure = configure;
    }

    /**
     * Spring 容器启动时初始化连接池
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 创建 Producer 池配置
        GenericObjectPoolConfig poolConfig =
                new GenericObjectPoolConfig();

        // 核心参数：根据实际情况调整
        poolConfig.setMaxTotal(20);              // 最大连接数
        poolConfig.setMaxIdle(10);               // 最大空闲连接
        poolConfig.setMinIdle(5);                // 最小空闲连接
        poolConfig.setTestOnBorrow(true);        // 借用时验证
        poolConfig.setTestOnReturn(true);        // 归还时验证
        poolConfig.setMaxWaitMillis(30000);      // 最长等待时间（30秒）
        poolConfig.setTestWhileIdle(true);       // 空闲时定期验证
        poolConfig.setTimeBetweenEvictionRunsMillis(60000);  // 60秒检查一次
        poolConfig.setMinEvictableIdleTimeMillis(300000);    // 5分钟未使用则驱逐

        // 创建 Producer 工厂
        MQProducerFactory producerFactory = new MQProducerFactory(configure);

        // 初始化连接池
        this.producerPool = new GenericObjectPool<>(producerFactory, poolConfig);

        this.initialized = true;
        logger.info("MQClientConnectionPool initialized successfully");
    }

    /**
     * 从池中获取 Producer
     *
     * @return DefaultMQProducer 实例
     * @throws Exception 如果池已满或其他异常
     */
    public DefaultMQProducer borrowProducer() throws Exception {
        if (!initialized) {
            throw new IllegalStateException("Connection pool not initialized");
        }

        DefaultMQProducer producer = producerPool.borrowObject();
        logger.debug("Borrowed producer from pool, available: {}",
                producerPool.getNumIdle());
        return producer;
    }

    /**
     * 将 Producer 归还到池
     *
     * @param producer 要归还的 Producer
     */
    public void returnProducer(DefaultMQProducer producer) throws Exception {
        if (producer != null && initialized) {
            producerPool.returnObject(producer);
            logger.debug("Returned producer to pool, available: {}",
                    producerPool.getNumIdle());
        }
    }

    /**
     * 失效一个 Producer（从池中移除，不再使用）
     * <p>
     * 适用场景：检测到连接不可用时
     */
    public void invalidateProducer(DefaultMQProducer producer) throws Exception {
        if (producer != null && initialized) {
            producerPool.invalidateObject(producer);
            logger.warn("Invalidated unhealthy producer from pool");
        }
    }

    /**
     * 获取事务 Producer
     * <p>
     * 注意：事务 Producer 不使用池，每次都是新建或从队列获取
     * 原因：TransactionMQProducer 需要设置单独的 TransactionListener
     */
    public TransactionMQProducer borrowTransactionProducer() throws Exception {
        // 优先从队列获取可复用的
        TransactionMQProducer producer = transactionProducerQueue.poll();

        if (producer == null) {
            // 队列为空，创建新的
            producer = createTransactionProducer();
            logger.debug("Created new TransactionMQProducer");
        } else {
            logger.debug("Reused TransactionMQProducer from queue");
        }

        return producer;
    }

    /**
     * 归还事务 Producer（用于复用）
     */
    public void returnTransactionProducer(TransactionMQProducer producer)
            throws Exception {
        if (producer != null) {
            // 尝试放回队列（如果队列满则自动丢弃）
            boolean offered = transactionProducerQueue.offer(producer);
            if (!offered) {
                // 队列满，直接关闭
                producer.shutdown();
                logger.debug("Transaction producer queue is full, closed producer");
            } else {
                logger.debug("Returned TransactionMQProducer to queue");
            }
        }
    }

    /**
     * 创建新的 TransactionMQProducer
     */
    private TransactionMQProducer createTransactionProducer() {
        RPCHook rpcHook = buildRpcHook();

        TransactionMQProducer producer = new TransactionMQProducer(
                null,
                "console-transaction-producer",
                rpcHook,
                false,  // traceEnabled
                TopicValidator.RMQ_SYS_TRACE_TOPIC
        );

        producer.setNamesrvAddr(configure.getNamesrvAddr());
        producer.setUseTLS(configure.isUseTLS());
        producer.setInstanceName(generateInstanceName());

        try {
            producer.start();
        } catch (Exception e) {
            logger.error("Failed to start TransactionMQProducer", e);
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }

        return producer;
    }

    /**
     * 构建 RPC Hook（用于 ACL）
     */
    private RPCHook buildRpcHook() {
        if (configure.isACLEnabled() &&
                !configure.getAccessKey().isEmpty() &&
                !configure.getSecretKey().isEmpty()) {

            return new AclClientRPCHook(
                    new SessionCredentials(
                            configure.getAccessKey(),
                            configure.getSecretKey()
                    )
            );
        }
        return null;
    }

    /**
     * 生成唯一的 Instance 名称
     */
    private String generateInstanceName() {
        return "console-" + System.currentTimeMillis() +
                "-" + Thread.currentThread().getId();
    }

    /**
     * 获取连接池状态信息（用于监控）
     */
    public PoolStats getPoolStats() {
        if (!initialized) {
            return new PoolStats(0, 0, 0, 0);
        }

        return new PoolStats(
                producerPool.getNumActive(),     // 活跃连接数
                producerPool.getNumIdle(),       // 空闲连接数
                producerPool.getMaxTotal(),      // 最大连接数
                transactionProducerQueue.size()  // 事务Producer队列大小
        );
    }

    /**
     * 优雅关闭：清空所有连接
     */
    @Override
    public void destroy() throws Exception {
        if (initialized) {
            logger.info("Shutting down MQClientConnectionPool...");

            // 关闭 Producer 池
            if (producerPool != null) {
                producerPool.close();
                logger.info("Closed Producer connection pool");
            }

            // 关闭所有事务 Producer
            TransactionMQProducer transactionProducer;
            while ((transactionProducer = transactionProducerQueue.poll()) != null) {
                try {
                    transactionProducer.shutdown();
                } catch (Exception e) {
                    logger.warn("Error shutting down TransactionMQProducer", e);
                }
            }

            initialized = false;
            logger.info("MQClientConnectionPool shutdown completed");
        }
    }

    /**
         * 池状态统计
         */
        public record PoolStats(int activeConnections, int idleConnections, int maxConnections,
                                int transactionProducerQueueSize) {

        @Override
            public String toString() {
                return String.format(
                        "PoolStats{active=%d, idle=%d, max=%d, txQueue=%d}",
                        activeConnections, idleConnections, maxConnections,
                        transactionProducerQueueSize
                );
            }
        }
}
