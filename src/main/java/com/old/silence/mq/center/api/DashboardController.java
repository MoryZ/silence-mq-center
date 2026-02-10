package com.old.silence.mq.center.api;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.old.silence.mq.center.domain.service.DashboardService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping(value = "/broker")
    public Map<String, List<String>> broker(@RequestParam String date) {
        return dashboardService.queryBrokerData(date);
    }

    @GetMapping(value = "/topic", params = {"date"})
    public Map<String, List<String>> topic(@RequestParam String date) {
        return dashboardService.queryTopicData(date);
    }

    @GetMapping(value = "/topic", params = {"date", "topicName"})
    public List<String> topicWithTopicName(@RequestParam String date, @RequestParam String topicName) {
        return dashboardService.queryTopicData(date, topicName);
    }

    @GetMapping(value = "/topicCurrent")
    public List<String> topicCurrent() {
        return dashboardService.queryTopicCurrentData();
    }

}
