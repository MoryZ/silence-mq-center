

package com.old.silence.mq.center.domain.service;

import org.apache.rocketmq.remoting.protocol.body.ProducerConnection;

public interface ProducerService {
    ProducerConnection getProducerConnection(String producerGroup, String topic);
}
