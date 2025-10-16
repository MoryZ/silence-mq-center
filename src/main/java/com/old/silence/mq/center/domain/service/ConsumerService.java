

package com.old.silence.mq.center.domain.service;

import org.apache.rocketmq.remoting.protocol.body.ConsumerConnection;
import org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo;
import com.old.silence.mq.center.domain.model.ConsumerGroupRollBackStat;
import com.old.silence.mq.center.domain.model.GroupConsumeInfo;
import com.old.silence.mq.center.domain.model.TopicConsumerInfo;
import com.old.silence.mq.center.domain.model.request.ConsumerConfigInfo;
import com.old.silence.mq.center.domain.model.request.DeleteSubGroupRequest;
import com.old.silence.mq.center.domain.model.request.ResetOffsetRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ConsumerService {
    List<GroupConsumeInfo> queryGroupList(boolean skipSysGroup, String address);

    GroupConsumeInfo queryGroup(String consumerGroup, String address);

    GroupConsumeInfo refreshGroup(String address, String consumerGroup);

    List<GroupConsumeInfo> refreshAllGroup(String address);

    List<TopicConsumerInfo> queryConsumeStatsListByGroupName(String groupName, String address);

    List<TopicConsumerInfo> queryConsumeStatsList(String topic, String groupName);

    Map<String, TopicConsumerInfo> queryConsumeStatsListByTopicName(String topic);

    Map<String /*consumerGroup*/, ConsumerGroupRollBackStat> resetOffset(ResetOffsetRequest resetOffsetRequest);

    List<ConsumerConfigInfo> examineSubscriptionGroupConfig(String group);

    boolean deleteSubGroup(DeleteSubGroupRequest deleteSubGroupRequest);

    boolean createAndUpdateSubscriptionGroupConfig(ConsumerConfigInfo consumerConfigInfo);

    Set<String> fetchBrokerNameSetBySubscriptionGroup(String group);

    ConsumerConnection getConsumerConnection(String consumerGroup, String address);

    ConsumerRunningInfo getConsumerRunningInfo(String consumerGroup, String clientId, boolean jstack);
}
