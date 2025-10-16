
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
