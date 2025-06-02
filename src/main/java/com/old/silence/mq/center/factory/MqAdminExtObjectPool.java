/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
