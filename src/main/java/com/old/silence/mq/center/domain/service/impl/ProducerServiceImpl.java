

package com.old.silence.mq.center.domain.service.impl;

import org.apache.rocketmq.remoting.protocol.body.ProducerConnection;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.springframework.stereotype.Service;
import com.google.common.base.Throwables;
import com.old.silence.mq.center.domain.service.ProducerService;

@Service
public class ProducerServiceImpl implements ProducerService {
    private final MQAdminExt mqAdminExt;

    public ProducerServiceImpl(MQAdminExt mqAdminExt) {
        this.mqAdminExt = mqAdminExt;
    }

    @Override
    public ProducerConnection getProducerConnection(String producerGroup, String topic) {
        try {
            return mqAdminExt.examineProducerConnectionInfo(producerGroup, topic);
        }
        catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }
}
