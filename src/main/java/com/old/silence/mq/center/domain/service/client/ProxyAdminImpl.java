
package com.old.silence.mq.center.domain.service.client;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.remoting.RemotingClient;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.body.ConsumerConnection;
import org.apache.rocketmq.remoting.protocol.header.GetConsumerConnectionListRequestHeader;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static org.apache.rocketmq.remoting.protocol.RequestCode.GET_CONSUMER_CONNECTION_LIST;

@Service
public class ProxyAdminImpl implements ProxyAdmin {


    private final GenericObjectPool<MQAdminExt> mqAdminExtPool;

    public ProxyAdminImpl(GenericObjectPool<MQAdminExt> mqAdminExtPool) {
        this.mqAdminExtPool = mqAdminExtPool;
    }

    @Override
    public ConsumerConnection examineConsumerConnectionInfo(String addr, String consumerGroup) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException, MQBrokerException {
        try {
            MQAdminInstance.createMQAdmin(mqAdminExtPool);
            RemotingClient remotingClient = MQAdminInstance.threadLocalRemotingClient();
            GetConsumerConnectionListRequestHeader requestHeader = new GetConsumerConnectionListRequestHeader();
            requestHeader.setConsumerGroup(consumerGroup);
            RemotingCommand request = RemotingCommand.createRequestCommand(GET_CONSUMER_CONNECTION_LIST, requestHeader);
            RemotingCommand response = remotingClient.invokeSync(addr, request, 3000);
            if (response.getCode() == 0) {
                return ConsumerConnection.decode(response.getBody(), ConsumerConnection.class);
            }
            throw new MQBrokerException(response.getCode(), response.getRemark(), addr);
        } finally {
            MQAdminInstance.returnMQAdmin(mqAdminExtPool);
        }
    }
}
