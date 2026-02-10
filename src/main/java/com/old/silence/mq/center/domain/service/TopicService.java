package com.old.silence.mq.center.domain.service;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.body.GroupList;
import org.apache.rocketmq.remoting.protocol.body.TopicList;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import com.old.silence.mq.center.domain.model.request.SendTopicMessageRequest;
import com.old.silence.mq.center.domain.model.request.TopicConfigInfo;
import com.old.silence.mq.center.domain.model.request.TopicTypeList;

import java.util.List;

public interface TopicService {
    TopicList fetchAllTopicList(boolean skipSysProcess, boolean skipRetryAndDlq);

    TopicTypeList examineAllTopicType();

    TopicStatsTable stats(String topic);

    TopicRouteData route(String topic);

    GroupList queryTopicConsumerInfo(String topic);

    void createOrUpdate(TopicConfigInfo topicCreateOrUpdateRequest);

    TopicConfig examineTopicConfig(String topic, String brokerName);

    List<TopicConfigInfo> examineTopicConfig(String topic);

    boolean deleteTopic(String topic, String clusterName);

    boolean deleteTopic(String topic);

    boolean deleteTopicInBroker(String brokerName, String topic);

    SendResult sendTopicMessageRequest(SendTopicMessageRequest sendTopicMessageRequest);

    boolean refreshTopicList();

}
