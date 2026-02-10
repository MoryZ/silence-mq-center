package com.old.silence.mq.center.domain.service.impl;

import org.apache.rocketmq.common.attribute.TopicMessageType;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.google.common.collect.Maps;
import com.old.silence.mq.center.domain.service.ClusterService;
import com.old.silence.mq.center.domain.service.facade.RocketMQClientFacade;
import com.old.silence.mq.center.util.JsonUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
public class ClusterServiceImpl implements ClusterService {
    private final Logger logger = LoggerFactory.getLogger(ClusterServiceImpl.class);
    private final RocketMQClientFacade mqFacade;

    public ClusterServiceImpl(RocketMQClientFacade mqFacade) {
        this.mqFacade = mqFacade;
    }

    @Override
    public Map<String, Object> list() {
        try {
            Map<String, Object> resultMap = Maps.newHashMap();
            ClusterInfo clusterInfo = mqFacade.getRawClusterInfo();
            logger.info("op=look_clusterInfo {}", JsonUtil.obj2String(clusterInfo));
            Map<String/*brokerName*/, Map<Long/* brokerId */, Object/* brokerDetail */>> brokerServer = Maps.newHashMap();
            for (BrokerData brokerData : clusterInfo.getBrokerAddrTable().values()) {
                Map<Long, Object> brokerMasterSlaveMap = Maps.newHashMap();
                for (Map.Entry<Long/* brokerId */, String/* broker address */> brokerAddr : brokerData.getBrokerAddrs().entrySet()) {
                    Map<String, String> brokerStats = mqFacade.fetchBrokerRuntimeStats(brokerAddr.getValue()).getTable();
                    brokerMasterSlaveMap.put(brokerAddr.getKey(), brokerStats);
                }
                brokerServer.put(brokerData.getBrokerName(), brokerMasterSlaveMap);
            }
            resultMap.put("clusterInfo", clusterInfo);
            resultMap.put("brokerServer", brokerServer);
            // add messageType
            resultMap.put("messageTypes", Arrays.stream(TopicMessageType.values()).sorted()
                    .collect(Collectors.toMap(TopicMessageType::getValue, messageType -> String.format("MESSAGE_TYPE_%s", messageType.getValue()))));
            return resultMap;
        } catch (Exception err) {
            logger.error("op=list failed", err);
            throw new RuntimeException(err);
        }
    }


    @Override
    public Properties getBrokerConfig(String brokerAddr) {
        try {
            return mqFacade.getBrokerConfig(brokerAddr);
        } catch (Exception e) {
            logger.error("op=getBrokerConfig failed, brokerAddr={}", brokerAddr, e);
            throw new RuntimeException(e);
        }
    }
}
