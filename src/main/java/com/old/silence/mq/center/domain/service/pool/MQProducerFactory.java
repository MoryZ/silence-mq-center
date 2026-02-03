package com.old.silence.mq.center.domain.service.pool;

import com.google.common.base.Throwables;
import com.old.silence.mq.center.api.config.RMQConfigure;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.topic.TopicValidator;
import org.apache.rocketmq.remoting.RPCHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Producer 对象工厂
 * 
 * 职责：创建、初始化、验证和销毁 DefaultMQProducer 对象
 * 由 GenericObjectPool 管理其生命周期
 * 
 * 关键点：
 * 1. makeObject - 创建新 Producer 时调用（包括启动）
 * 2. destroyObject - 销毁 Producer 时调用（包括关闭）
 * 3. validateObject - 验证 Producer 是否可用
 * 4. activateObject / passivateObject - 生命周期钩子
 */
public class MQProducerFactory implements PooledObjectFactory<DefaultMQProducer> {
    
    private static final Logger logger = LoggerFactory.getLogger(MQProducerFactory.class);
    
    private final RMQConfigure configure;
    private final AtomicInteger producerIdGenerator = new AtomicInteger(0);
    
    public MQProducerFactory(RMQConfigure configure) {
        this.configure = configure;
    }
    
    /**
     * 创建并初始化新的 Producer 对象
     * 
     * 这个方法在以下情况被调用：
     * 1. 连接池初始化时
     * 2. 需要扩展池大小时
     * 3. 手动添加新对象时
     * 
     * 注意：此方法中会启动 Producer，这是关键的一次性操作
     */
    @Override
    public PooledObject<DefaultMQProducer> makeObject() throws Exception {
        try {
            DefaultMQProducer producer = new DefaultMQProducer(
                "console-producer-group",
                buildRpcHook(),
                false,  // traceEnabled
                TopicValidator.RMQ_SYS_TRACE_TOPIC
            );
            
            // 配置 Producer
            producer.setNamesrvAddr(configure.getNamesrvAddr());
            producer.setUseTLS(configure.isUseTLS());
            producer.setInstanceName(generateInstanceName());
            
            // ✅ 关键：启动 Producer 只在这里做一次
            // 不像之前的代码在每次使用时都启动和关闭
            producer.start();
            
            logger.info("Created and started new Producer: {}", 
                producer.getProducerGroup());
            
            return new DefaultPooledObject<>(producer);
            
        } catch (Exception e) {
            logger.error("Failed to create Producer", e);
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException("Failed to create Producer", e);
        }
    }
    
    /**
     * 销毁 Producer 对象
     * 
     * 调用场景：
     * 1. 连接池关闭时
     * 2. 驱逐空闲连接时
     * 3. 对象无效时
     */
    @Override
    public void destroyObject(PooledObject<DefaultMQProducer> p) throws Exception {
        DefaultMQProducer producer = p.getObject();
        if (producer != null) {
            try {
                producer.shutdown();
                logger.info("Destroyed Producer: {}", producer.getProducerGroup());
            } catch (Exception e) {
                logger.warn("Error shutting down Producer", e);
            }
        }
    }
    
    /**
     * 验证 Producer 是否可用
     * 
     * 在以下时机被调用：
     * 1. testOnBorrow: 从池中借用时
     * 2. testOnReturn: 归还时
     * 3. testWhileIdle: 后台空闲检查时
     * 
     * 如果返回 false，该对象会被驱逐并重新创建
     */
    @Override
    public boolean validateObject(PooledObject<DefaultMQProducer> p) {
        DefaultMQProducer producer = p.getObject();
        
        if (producer == null) {
            logger.warn("Producer is null");
            return false;
        }
        
        // 检查 Producer 状态
        try {
            // 检查是否已启动
            if (!producer.getDefaultMQProducerImpl().isServiceStateOk()) {
                logger.warn("Producer service state is not OK");
                return false;
            }
            
            // 可以加更多的健康检查逻辑
            // 例如：ping NameServer、检查网络连接等
            
            return true;
            
        } catch (Exception e) {
            logger.warn("Validation failed for producer", e);
            return false;
        }
    }
    
    /**
     * 对象被激活时调用（从池中取出时）
     * 
     * 可以在这里进行重置或预处理
     */
    @Override
    public void activateObject(PooledObject<DefaultMQProducer> p) throws Exception {
        // 可选：在这里做一些重置工作
        logger.debug("Activated Producer: {}", p.getObject().getProducerGroup());
    }
    
    /**
     * 对象被钝化时调用（归还到池时）
     * 
     * 可以在这里清理对象状态
     */
    @Override
    public void passivateObject(PooledObject<DefaultMQProducer> p) throws Exception {
        // 可选：在这里清理对象状态
        logger.debug("Passivated Producer: {}", p.getObject().getProducerGroup());
    }
    
    /**
     * 构建 RPC Hook（用于 ACL 认证）
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
     * 
     * 重要：每个 Producer 需要独立的名称，避免冲突
     */
    private String generateInstanceName() {
        int producerId = producerIdGenerator.incrementAndGet();
        return String.format("console-producer-%d-jvm-%d", 
            producerId, 
            Thread.currentThread().getId());
    }
}
