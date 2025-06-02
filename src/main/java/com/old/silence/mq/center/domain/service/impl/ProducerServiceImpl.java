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

package com.old.silence.mq.center.domain.service.impl;

import org.apache.rocketmq.remoting.protocol.body.ProducerConnection;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.springframework.stereotype.Service;
import com.google.common.base.Throwables;
import com.old.silence.mq.center.domain.service.ProducerService;

@Service
public class ProducerServiceImpl implements ProducerService {
    private final MQAdminExt mqAdminExt;

    public ProducerServiceImpl(MQAdminExt mqAdminExt) {
        this.mqAdminExt = mqAdminExt;
    }

    @Override
    public ProducerConnection getProducerConnection(String producerGroup, String topic) {
        try {
            return mqAdminExt.examineProducerConnectionInfo(producerGroup, topic);
        }
        catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }
}
