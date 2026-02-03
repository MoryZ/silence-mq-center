

package com.old.silence.mq.center.domain.service.impl;

import org.apache.rocketmq.remoting.protocol.body.ProducerConnection;
import org.springframework.stereotype.Service;
import com.old.silence.mq.center.domain.service.ProducerService;
import com.old.silence.mq.center.domain.service.facade.RocketMQClientFacade;

@Service
public class ProducerServiceImpl implements ProducerService {
    private final RocketMQClientFacade mqFacade;

    public ProducerServiceImpl(RocketMQClientFacade mqFacade) {
        this.mqFacade = mqFacade;
    }

    @Override
    public ProducerConnection getProducerConnection(String producerGroup, String topic) {
        return mqFacade.getProducerConnection(producerGroup, topic);
    }
}
