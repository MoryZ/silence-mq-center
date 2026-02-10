package com.old.silence.mq.center.api;

import org.apache.rocketmq.remoting.protocol.body.ProducerConnection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.old.silence.mq.center.domain.model.ConnectionInfo;
import com.old.silence.mq.center.domain.service.ProducerService;

@RestController
@RequestMapping("/api/v1/producer")
public class ProducerController {

    private final ProducerService producerService;

    public ProducerController(ProducerService producerService) {
        this.producerService = producerService;
    }

    @GetMapping(value = "/producerConnection")
    public ProducerConnection producerConnection(@RequestParam String producerGroup, @RequestParam String topic) {
        ProducerConnection producerConnection = producerService.getProducerConnection(producerGroup, topic);
        producerConnection.setConnectionSet(ConnectionInfo.buildConnectionInfoHashSet(producerConnection.getConnectionSet()));
        return producerConnection;
    }
}
