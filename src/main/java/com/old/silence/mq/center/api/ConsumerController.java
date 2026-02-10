package com.old.silence.mq.center.api;

import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.remoting.protocol.body.ConsumerConnection;
import org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.google.common.base.Preconditions;
import com.old.silence.mq.center.domain.model.ConnectionInfo;
import com.old.silence.mq.center.domain.model.ConsumerGroupRollBackStat;
import com.old.silence.mq.center.domain.model.GroupConsumeInfo;
import com.old.silence.mq.center.domain.model.TopicConsumerInfo;
import com.old.silence.mq.center.domain.model.request.ConsumerConfigInfo;
import com.old.silence.mq.center.domain.model.request.DeleteSubGroupRequest;
import com.old.silence.mq.center.domain.model.request.ResetOffsetRequest;
import com.old.silence.mq.center.domain.service.ConsumerService;
import com.old.silence.mq.center.util.JsonUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/consumer")
public class ConsumerController {
    private final Logger logger = LoggerFactory.getLogger(ConsumerController.class);

    private final ConsumerService consumerService;

    public ConsumerController(ConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    @GetMapping(value = "/groupList")
    public List<GroupConsumeInfo> list(@RequestParam(value = "skipSysGroup", required = false) boolean skipSysGroup, String address) {
        return consumerService.queryGroupList(skipSysGroup, address);
    }

    @GetMapping(value = "/group/refresh")
    public GroupConsumeInfo refresh(String address,
                                    String consumerGroup) {
        return consumerService.refreshGroup(address, consumerGroup);
    }

    @GetMapping(value = "/group/refreshAll")
    public List<GroupConsumeInfo> refreshAll(String address) {
        return consumerService.refreshAllGroup(address);
    }

    @GetMapping(value = "/group")
    public GroupConsumeInfo groupQuery(@RequestParam String consumerGroup, String address) {
        return consumerService.queryGroup(consumerGroup, address);
    }

    @PostMapping(value = "/resetOffset")
    public Map<String, ConsumerGroupRollBackStat> resetOffset(@RequestBody ResetOffsetRequest resetOffsetRequest) {
        logger.info("op=look resetOffsetRequest:{}", JsonUtil.obj2String(resetOffsetRequest));
        return consumerService.resetOffset(resetOffsetRequest);
    }

    @PostMapping(value = "/skipAccumulate")
    public Map<String, ConsumerGroupRollBackStat> skipAccumulate(@RequestBody ResetOffsetRequest resetOffsetRequest) {
        logger.info("op=look resetOffsetRequest:{}", JsonUtil.obj2String(resetOffsetRequest));
        return consumerService.resetOffset(resetOffsetRequest);
    }

    @GetMapping(value = "/examineSubscriptionGroupConfig")
    public List<ConsumerConfigInfo> examineSubscriptionGroupConfig(@RequestParam String consumerGroup) {
        return consumerService.examineSubscriptionGroupConfig(consumerGroup);
    }

    @DeleteMapping(value = "/deleteSubGroup")
    public Boolean deleteSubGroup(@RequestBody DeleteSubGroupRequest deleteSubGroupRequest) {
        consumerService.deleteSubGroup(deleteSubGroupRequest);
        return true;
    }

    @PostMapping(value = "/createOrUpdate")
    public Boolean consumerCreateOrUpdateRequest(@RequestBody ConsumerConfigInfo consumerConfigInfo) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(consumerConfigInfo.getBrokerNameList()) || CollectionUtils.isNotEmpty(consumerConfigInfo.getClusterNameList()),
                "clusterName or brokerName can not be all blank");
        return consumerService.createAndUpdateSubscriptionGroupConfig(consumerConfigInfo);
    }

    @GetMapping(value = "/fetchBrokerNameList")
    public Set<String> fetchBrokerNameList(@RequestParam String consumerGroup) {
        return consumerService.fetchBrokerNameSetBySubscriptionGroup(consumerGroup);
    }

    @GetMapping(value = "/queryTopicByConsumer")
    public List<TopicConsumerInfo> queryConsumerByTopic(@RequestParam String consumerGroup, String address) {
        return consumerService.queryConsumeStatsListByGroupName(consumerGroup, address);
    }

    @GetMapping(value = "/consumerConnection")
    public ConsumerConnection consumerConnection(@RequestParam(required = false) String consumerGroup, String address) {
        ConsumerConnection consumerConnection = consumerService.getConsumerConnection(consumerGroup, address);
        consumerConnection.setConnectionSet(ConnectionInfo.buildConnectionInfoHashSet(consumerConnection.getConnectionSet()));
        return consumerConnection;
    }

    @GetMapping(value = "/consumerRunningInfo")
    public ConsumerRunningInfo getConsumerRunningInfo(@RequestParam String consumerGroup, @RequestParam String clientId,
                                                      @RequestParam boolean jstack) {
        return consumerService.getConsumerRunningInfo(consumerGroup, clientId, jstack);
    }
}
