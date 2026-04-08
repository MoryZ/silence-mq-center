package com.old.silence.mq.center.api;

import org.apache.rocketmq.client.ClientConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.old.silence.mq.center.domain.service.MQAdminService;

@RestController
@RequestMapping("/api/v1/rocketmq")
public class NamesvrController {
    private final MQAdminService mqAdminService;

    public NamesvrController(MQAdminService mqAdminService) {
        this.mqAdminService = mqAdminService;
    }

    @GetMapping(value = "/nsaddr")
    public String nsAddr() throws Exception {
        // 获取客户端实例
        // admin.getInternalAddr() 在 5.x 中有时能拿到，但最稳妥的是通过 NamesrvAddr
        return mqAdminService.execute(ClientConfig::getNamesrvAddr);
    }
}
