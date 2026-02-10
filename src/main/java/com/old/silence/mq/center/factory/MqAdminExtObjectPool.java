package com.old.silence.mq.center.factory;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.old.silence.mq.center.api.config.RMQConfigure;

@Configuration
public class MqAdminExtObjectPool {

    private final RMQConfigure rmqConfigure;

    public MqAdminExtObjectPool(RMQConfigure rmqConfigure) {
        this.rmqConfigure = rmqConfigure;
    }

    @Bean
    public GenericObjectPool<MQAdminExt> mqAdminExtPool() {
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        genericObjectPoolConfig.setTestWhileIdle(true);
        genericObjectPoolConfig.setMaxWaitMillis(10000);
        genericObjectPoolConfig.setTimeBetweenEvictionRunsMillis(20000);
        genericObjectPoolConfig.setJmxEnabled(false);

        MQAdminFactory mqAdminFactory = new MQAdminFactory(rmqConfigure);
        MQAdminPooledObjectFactory mqAdminPooledObjectFactory = new MQAdminPooledObjectFactory(mqAdminFactory);
        return new GenericObjectPool<>(
                mqAdminPooledObjectFactory,
                genericObjectPoolConfig);
    }
}
