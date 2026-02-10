package com.old.silence.mq.center.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.old.silence.mq.center.domain.service.ClusterService;

import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping("/api/v1/cluster")
public class ClusterController {

    private final ClusterService clusterService;

    public ClusterController(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @GetMapping(value = "/list")
    public Map<String, Object> list() {
        return clusterService.list();
    }

    @GetMapping(value = "/brokerConfig")
    public Properties brokerConfig(@RequestParam String brokerAddr) {
        return clusterService.getBrokerConfig(brokerAddr);
    }
}
