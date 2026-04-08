package com.old.silence.mq.center.domain.service;

import org.apache.rocketmq.remoting.protocol.body.ProducerConnection;
import org.springframework.stereotype.Service;

/**
 * @author moryzang
 */
@Service
public class ProducerService {

    private final MQAdminService mqAdminService;

    public ProducerService(MQAdminService mqAdminService) {
        this.mqAdminService = mqAdminService;
    }

    public ProducerConnection getProducerConnection(String producerGroup, String topic) throws Exception {
        return mqAdminService.execute(admin -> admin.examineProducerConnectionInfo(producerGroup, topic));
    }
}
