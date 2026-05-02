package com.old.silence.mq.center.domain.service;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.common.attribute.TopicMessageType;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.apache.rocketmq.remoting.protocol.body.KVTable;
import org.springframework.stereotype.Service;

import com.old.silence.mq.center.vo.ClusterDetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author moryzang
 */
@Service
public class ClusterService {

    private final MQAdminService mqAdminService;

    public ClusterService(MQAdminService mqAdminService) {
        this.mqAdminService = mqAdminService;
    }

    public ClusterDetail list() throws Exception {
        ClusterDetail clusterDetail = new ClusterDetail();
        mqAdminService.executeVoid(admin -> {

            var clusterInfo = admin.examineBrokerClusterInfo();
            clusterDetail.setClusterInfo(clusterInfo);

            Map<String, Map<Long, Object>> brokerServer = new HashMap<>();
            clusterInfo.getBrokerAddrTable().values().forEach(brokerData -> {
                Map<Long, Object> brokerMasterSlaveMap = new HashMap<>();
               brokerData.getBrokerAddrs().forEach((key, value) -> {
                   KVTable kvTable;
                   try {
                       kvTable = admin.fetchBrokerRuntimeStats(value);
                   } catch (RemotingConnectException | RemotingSendRequestException | RemotingTimeoutException |
                            InterruptedException | MQBrokerException e) {
                       throw new RuntimeException(e);
                   }
                   brokerMasterSlaveMap.put(key, kvTable.getTable());
               });
                brokerServer.put(brokerData.getBrokerName(), brokerMasterSlaveMap);

            });
            clusterDetail.setBrokerServer(brokerServer);

        });
        clusterDetail.setMessageTypes(new ArrayList<>(TopicMessageType.topicMessageTypeSet()));
        return clusterDetail;
    }

    public Properties getBrokerConfig(String brokerAddr) throws Exception {
        return mqAdminService.execute(admin -> admin.getBrokerConfig(brokerAddr));
    }
}
