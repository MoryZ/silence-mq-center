package com.old.silence.mq.center.domain.service;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.common.AclConfig;
import org.apache.rocketmq.common.PlainAccessConfig;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import com.old.silence.mq.center.domain.model.request.AclRequest;

import java.util.List;

public interface AclService {

    AclConfig getAclConfig(boolean excludeSecretKey);

    void addAclConfig(PlainAccessConfig config);

    void deleteAclConfig(PlainAccessConfig config) throws RemotingSendRequestException, RemotingConnectException, RemotingTimeoutException, MQBrokerException, InterruptedException;

    void updateAclConfig(PlainAccessConfig config) throws RemotingSendRequestException, RemotingConnectException, RemotingTimeoutException, MQBrokerException, InterruptedException;

    void addOrUpdateAclTopicConfig(AclRequest request) throws RemotingSendRequestException, RemotingConnectException, RemotingTimeoutException, MQBrokerException, InterruptedException;

    void addOrUpdateAclGroupConfig(AclRequest request) throws RemotingSendRequestException, RemotingConnectException, RemotingTimeoutException, MQBrokerException, InterruptedException;

    void deletePermConfig(AclRequest request);

    void syncData(PlainAccessConfig config);

    void addWhiteList(List<String> whiteList) throws RemotingSendRequestException, RemotingConnectException, RemotingTimeoutException, MQBrokerException, InterruptedException;

    void deleteWhiteAddr(String addr) throws RemotingSendRequestException, RemotingConnectException, RemotingTimeoutException, MQBrokerException, InterruptedException;

    void synchronizeWhiteList(List<String> whiteList) throws RemotingSendRequestException, RemotingConnectException, RemotingTimeoutException, MQBrokerException, InterruptedException;
}
