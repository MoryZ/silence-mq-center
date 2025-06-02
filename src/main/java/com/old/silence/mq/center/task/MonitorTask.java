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
package com.old.silence.mq.center.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.old.silence.mq.center.domain.model.ConsumerMonitorConfig;
import com.old.silence.mq.center.domain.model.GroupConsumeInfo;
import com.old.silence.mq.center.domain.service.ConsumerService;
import com.old.silence.mq.center.domain.service.MonitorService;
import com.old.silence.mq.center.util.JsonUtil;

import java.util.Map;

@Component
public class MonitorTask {
    private final Logger logger = LoggerFactory.getLogger(MonitorTask.class);

    private final MonitorService monitorService;

    private final ConsumerService consumerService;

    public MonitorTask(MonitorService monitorService, ConsumerService consumerService) {
        this.monitorService = monitorService;
        this.consumerService = consumerService;
    }

    //    @Scheduled(cron = "* * * * * ?")
    public void scanProblemConsumeGroup() {
        for (Map.Entry<String, ConsumerMonitorConfig> configEntry : monitorService.queryConsumerMonitorConfig().entrySet()) {
            GroupConsumeInfo consumeInfo = consumerService.queryGroup(configEntry.getKey(), null);
            if (consumeInfo.getCount() < configEntry.getValue().getMinCount() || consumeInfo.getDiffTotal() > configEntry.getValue().getMaxDiffTotal()) {
                logger.info("op=look consumeInfo {}", JsonUtil.obj2String(consumeInfo)); // notify the alert system
            }
        }
    }

}
