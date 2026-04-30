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
import com.old.silence.json.JacksonMapper;
import com.old.silence.mq.center.dto.ConnectionInfo;
import com.old.silence.mq.center.dto.ConsumerGroupRollBackStat;
import com.old.silence.mq.center.dto.GroupConsumeInfo;
import com.old.silence.mq.center.dto.TopicConsumerInfo;
import com.old.silence.mq.center.dto.ConsumerConfigInfo;
import com.old.silence.mq.center.dto.DeleteSubGroupRequest;
import com.old.silence.mq.center.dto.ResetOffsetRequest;
import com.old.silence.mq.center.domain.service.ConsumerService;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/consumer")
public class ConsumerController {
    private final Logger logger = LoggerFactory.getLogger(ConsumerController.class);

    private final ConsumerService consumerService;
    private final JacksonMapper jacksonMapper;

    public ConsumerController(ConsumerService consumerService, JacksonMapper jacksonMapper) {
        this.consumerService = consumerService;
        this.jacksonMapper = jacksonMapper;
    }

    @GetMapping(value = "/groupList")
    public List<GroupConsumeInfo> list(@RequestParam(value = "skipSysGroup", required = false) boolean skipSysGroup, String address) throws Exception {
        return consumerService.queryGroupList(skipSysGroup, address);
    }

    @GetMapping(value = "/group/refresh")
    public GroupConsumeInfo refresh(String address,
                                    String consumerGroup) throws Exception {
        return consumerService.refreshGroup(address, consumerGroup);
    }

    @GetMapping(value = "/group/refreshAll")
    public List<GroupConsumeInfo> refreshAll(String address) throws Exception {
        return consumerService.refreshAllGroup(address);
    }

    @GetMapping(value = "/group")
    public GroupConsumeInfo groupQuery(@RequestParam String consumerGroup, String address) throws Exception {
        return consumerService.queryGroup(consumerGroup, address);
    }

    @PostMapping(value = "/resetOffset")
    public Map<String, ConsumerGroupRollBackStat> resetOffset(@RequestBody ResetOffsetRequest resetOffsetRequest) throws Exception {
        logger.info("op=look resetOffsetRequest:{}", jacksonMapper.toJson(resetOffsetRequest));
        return consumerService.resetOffset(resetOffsetRequest);
    }

    @PostMapping(value = "/skipAccumulate")
    public Map<String, ConsumerGroupRollBackStat> skipAccumulate(@RequestBody ResetOffsetRequest resetOffsetRequest) throws Exception {
        logger.info("op=look skipAccumulateRequest:{}", jacksonMapper.toJson(resetOffsetRequest));
        return consumerService.resetOffset(resetOffsetRequest);
    }

    @GetMapping(value = "/examineSubscriptionGroupConfig")
    public List<ConsumerConfigInfo> examineSubscriptionGroupConfig(@RequestParam String consumerGroup) throws Exception {
        return consumerService.examineSubscriptionGroupConfig(consumerGroup);
    }

    @DeleteMapping(value = "/deleteSubGroup")
    public Boolean deleteSubGroup(@RequestBody DeleteSubGroupRequest deleteSubGroupRequest) throws Exception {
        consumerService.deleteSubGroup(deleteSubGroupRequest);
        return true;
    }

    @PostMapping(value = "/createOrUpdate")
    public Boolean consumerCreateOrUpdateRequest(@RequestBody ConsumerConfigInfo consumerConfigInfo) throws Exception {
        return consumerService.createAndUpdateSubscriptionGroupConfig(consumerConfigInfo);
    }

    @GetMapping(value = "/fetchBrokerNameList")
    public Set<String> fetchBrokerNameList(@RequestParam String consumerGroup) throws Exception {
        return consumerService.fetchBrokerNameSetBySubscriptionGroup(consumerGroup);
    }

    @GetMapping(value = "/queryTopicByConsumer")
    public List<TopicConsumerInfo> queryConsumerByTopic(@RequestParam String consumerGroup, String brokerAddress) throws Exception {
        return consumerService.queryConsumeStatsListByGroupName(consumerGroup, brokerAddress);
    }

    @GetMapping(value = "/consumerConnection")
    public ConsumerConnection consumerConnection(@RequestParam(required = false) String consumerGroup, String brokerAddress) throws Exception {
        ConsumerConnection consumerConnection = consumerService.getConsumerConnection(consumerGroup, brokerAddress);
        consumerConnection.setConnectionSet(ConnectionInfo.buildConnectionInfoHashSet(consumerConnection.getConnectionSet()));
        return consumerConnection;
    }

    @GetMapping(value = "/consumerRunningInfo")
    public ConsumerRunningInfo getConsumerRunningInfo(@RequestParam String consumerGroup, @RequestParam String clientId,
                                                      @RequestParam boolean jstack) throws Exception {
        return consumerService.getConsumerRunningInfo(consumerGroup, clientId, jstack);
    }
}
