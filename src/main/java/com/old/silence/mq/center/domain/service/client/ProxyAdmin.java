
package com.old.silence.mq.center.domain.service.client;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.apache.rocketmq.remoting.protocol.body.ConsumerConnection;

public interface ProxyAdmin {

    ConsumerConnection examineConsumerConnectionInfo(String addr, String consumerGroup) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException, MQBrokerException;
}
