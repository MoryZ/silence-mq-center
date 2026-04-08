package com.old.silence.mq.center.api;

import java.util.Set;

import org.apache.rocketmq.remoting.protocol.body.Connection;
import org.apache.rocketmq.remoting.protocol.body.ProducerConnection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.old.silence.mq.center.dto.ConnectionInfo;
import com.old.silence.mq.center.domain.service.ProducerService;

@RestController
@RequestMapping("/api/v1")
public class ProducerController {

    private final ProducerService producerService;

    public ProducerController(ProducerService producerService) {
        this.producerService = producerService;
    }


    @GetMapping(value = "/producer")
    public Set<Connection> getProducerConnection(@RequestParam String producerGroup, @RequestParam String topic) throws Exception {
        // 1. 获取原始连接数据
        ProducerConnection pc = producerService.getProducerConnection(producerGroup, topic);

        // 2. 转换为前端友好的 DTO
        return ConnectionInfo.buildConnectionInfoHashSet(pc.getConnectionSet());
    }
}
