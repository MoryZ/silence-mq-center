package com.old.silence.mq.center.api;

import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.old.silence.mq.center.domain.service.MQAdminService;


@RestController
@RequestMapping("/api/v1")
public class ClusterController {

    private final MQAdminService mqAdminService;

    public ClusterController(MQAdminService mqAdminService) {
        this.mqAdminService = mqAdminService;
    }

    @GetMapping("/clusters")
    public ClusterInfo  getClusterList() throws Exception {
        // 获取集群元数据
        // 5.3.1 版本中，你可以从这里提取出所有 Broker 的地址、版本和流量数据
        return mqAdminService.execute(DefaultMQAdminExt::examineBrokerClusterInfo);
    }
}
