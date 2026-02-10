package com.old.silence.mq.center.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.old.silence.mq.center.domain.service.OpsService;

@RestController
@RequestMapping("/api/v1/rocketmq")
public class NamesvrController {
    private final OpsService opsService;

    public NamesvrController(OpsService opsService) {
        this.opsService = opsService;
    }

    @GetMapping(value = "/nsaddr")
    public String nsAddr() {
        return opsService.getNameSvrList();
    }
}
