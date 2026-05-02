package com.old.silence.mq.center.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.old.silence.mq.center.domain.service.MonitorService;
import com.old.silence.mq.center.dto.ConsumerMonitorConfig;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/monitor")
public class ConsumerMonitorController {

    private final MonitorService monitorService;

    public ConsumerMonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @PostMapping(value = "/createOrUpdateConsumerMonitor")
    public Boolean createOrUpdateConsumerMonitor(@RequestParam String consumeGroupName, @RequestParam int minCount,
                                                 @RequestParam int maxDiffTotal) throws Exception {
        return monitorService.createOrUpdateConsumerMonitor(consumeGroupName, minCount, maxDiffTotal);
    }

    @GetMapping(value = "/consumerMonitorConfig")
    public Map<String, ConsumerMonitorConfig> consumerMonitorConfig() throws Exception {
        return monitorService.queryConsumerMonitorConfig();
    }

    @GetMapping(value = "/consumerMonitorConfigByGroupName")
    public ConsumerMonitorConfig consumerMonitorConfigByGroupName(@RequestParam String consumeGroupName) throws Exception {
        return monitorService.queryConsumerMonitorConfigByGroupName(consumeGroupName);
    }

    @PostMapping(value = "/deleteConsumerMonitor")
    public Boolean deleteConsumerMonitor(@RequestParam String consumeGroupName) throws Exception {
        return monitorService.deleteConsumerMonitor(consumeGroupName);
    }
}
