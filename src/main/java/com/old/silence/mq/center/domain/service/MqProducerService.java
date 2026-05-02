package com.old.silence.mq.center.domain.service;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 管控台专用 Producer，用于 DLQ 消息重投等运维场景
 *
 * @author moryzang
 */
@Service
public class MqProducerService {

    private static final Logger log = LoggerFactory.getLogger(MqProducerService.class);

    @Value("${rocketmq.namesrv.addr:127.0.0.1:9876}")
    private String namesrvAddr;

    private DefaultMQProducer producer;

    @PostConstruct
    public void init() throws MQClientException {
        producer = new DefaultMQProducer("OP_CENTER_PRODUCER_GROUP");
        producer.setNamesrvAddr(namesrvAddr);
        producer.start();
        log.info("MqProducerService started, namesrvAddr={}", namesrvAddr);
    }

    /**
     * 发送消息到指定 topic
     *
     * @param topic 目标 topic
     * @param body  消息体
     * @return SendResult
     */
    public SendResult send(String topic, byte[] body) throws Exception {
        Message msg = new Message(topic, body);
        return producer.send(msg);
    }

    /**
     * 发送带完整属性的消息（保留原始 tags / keys / properties）
     *
     * @param msg 已构建好的消息对象
     * @return SendResult
     */
    public SendResult send(Message msg) throws Exception {
        return producer.send(msg);
    }

    @PreDestroy
    public void shutdown() {
        if (producer != null) {
            producer.shutdown();
            log.info("MqProducerService shutdown");
        }
    }
}
