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
package com.old.silence.mq.center.api;

import org.apache.rocketmq.remoting.protocol.body.ProducerConnection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.old.silence.mq.center.domain.model.ConnectionInfo;
import com.old.silence.mq.center.domain.service.ProducerService;

@RestController
@RequestMapping("/api/v1/producer")
public class ProducerController {

    private final ProducerService producerService;

    public ProducerController(ProducerService producerService) {
        this.producerService = producerService;
    }

    @GetMapping(value = "/producerConnection")
    public ProducerConnection producerConnection(@RequestParam String producerGroup, @RequestParam String topic) {
        ProducerConnection producerConnection = producerService.getProducerConnection(producerGroup, topic);
        producerConnection.setConnectionSet(ConnectionInfo.buildConnectionInfoHashSet(producerConnection.getConnectionSet()));
        return  producerConnection;
    }
}
