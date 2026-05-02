package com.old.silence.mq.center.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.old.silence.mq.center.domain.service.ClusterService;
import com.old.silence.mq.center.vo.ClusterDetail;

import java.util.Properties;


@RestController
@RequestMapping("/api/v1")
public class ClusterController {

    private final ClusterService clusterService;

    public ClusterController(ClusterService clusterService) {
        this.clusterService = clusterService;
    }


    @GetMapping("/clusters")
    public ClusterDetail getClusterList() throws Exception {
        return clusterService.list();
    }

    @GetMapping("/clusters/brokerConfig")
    public Properties brokerConfig(@RequestParam String brokerAddr) throws Exception {
        return clusterService.getBrokerConfig(brokerAddr);
    }
}
