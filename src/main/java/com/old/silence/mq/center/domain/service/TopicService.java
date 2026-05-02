package com.old.silence.mq.center.domain.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.attribute.TopicMessageType;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.body.GroupList;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.core.exception.ResourceNotFoundException;
import com.old.silence.mq.center.domain.model.Topic;
import com.old.silence.mq.center.domain.repository.TopicRepository;
import com.old.silence.mq.center.dto.SendTopicMessageCommand;
import com.old.silence.mq.center.dto.TopicConsumerInfoDetail;
import com.old.silence.mq.center.enums.MessageType;
import com.old.silence.mq.center.exception.ServiceException;
import com.old.silence.mq.center.vo.TopicConfigInfo;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class TopicService {

    private final MQAdminService mqAdminService;
    private final TopicRepository topicRepository;
    private final MqProducerService mqProducerService;

    public TopicService(MQAdminService mqAdminService,
                        TopicRepository topicRepository,
                        MqProducerService mqProducerService) {
        this.mqAdminService = mqAdminService;
        this.topicRepository = topicRepository;
        this.mqProducerService = mqProducerService;
    }

    public IPage<Topic> queryTopicPage(QueryWrapper<Topic> queryWrapper, Page<Topic> page) {
        return topicRepository.findByQuery(queryWrapper, page, Topic.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public BigInteger create(Topic topic) {
        try {
            topicRepository.insert(topic);
            createAndUpdateTopicConfig(topic);
            return topic.getId();
        } catch (Exception e) {
            throw new ServiceException(500, "Create topic failed: " + e.getMessage());
        }
    }

    private void createAndUpdateTopicConfig(Topic topic) throws Exception {
        createAndUpdateTopicConfig(topic, null);
    }

    private void createAndUpdateTopicConfig(Topic topic, List<String> preferredBrokerNames) throws Exception {
        ClusterInfo clusterInfo = mqAdminService.execute(DefaultMQAdminExt::examineBrokerClusterInfo);
        List<String> brokerAddrList = resolveBrokerAddrs(clusterInfo, preferredBrokerNames);
        if (brokerAddrList.isEmpty()) {
            throw new ServiceException(500, "No available broker address for topic: " + topic.getTopicName());
        }

        mqAdminService.executeVoid(admin -> {
            TopicConfig topicConfig = new TopicConfig(topic.getTopicName());
            topicConfig.setReadQueueNums(topic.getReadQueueNums());
            topicConfig.setWriteQueueNums(topic.getWriteQueueNums());
            // RocketMQ 5.3.1 必须设置消息类型
            topicConfig.getAttributes().put("+message.type", topic.getMessageType().getValue());

            for (String brokerAddr : brokerAddrList) {
                admin.createAndUpdateTopicConfig(brokerAddr, topicConfig);
            }
        });
    }

    private List<String> resolveBrokerAddrs(ClusterInfo clusterInfo, List<String> brokerNameList) {
        if (clusterInfo == null || clusterInfo.getBrokerAddrTable() == null) {
            throw new ServiceException(500, "Cluster info is empty");
        }

        Set<String> targetBrokerNames = new LinkedHashSet<>();
        if (brokerNameList != null) {
            for (String brokerName : brokerNameList) {
                if (StringUtils.isNotBlank(brokerName)) {
                    targetBrokerNames.add(brokerName);
                }
            }
        }

        // 未指定 broker 时，回退到当前集群中所有可见 broker
        if (targetBrokerNames.isEmpty()) {
            targetBrokerNames.addAll(clusterInfo.getBrokerAddrTable().keySet());
        }

        Set<String> brokerAddrSet = new LinkedHashSet<>();
        for (String brokerName : targetBrokerNames) {
            BrokerData brokerData = clusterInfo.getBrokerAddrTable().get(brokerName);
            if (brokerData == null) {
                continue;
            }
            String brokerAddr = brokerData.selectBrokerAddr();
            if (StringUtils.isBlank(brokerAddr) && brokerData.getBrokerAddrs() != null) {
                brokerAddr = brokerData.getBrokerAddrs().get(0L);
            }
            if (StringUtils.isNotBlank(brokerAddr)) {
                brokerAddrSet.add(brokerAddr);
            }
        }

        return new ArrayList<>(brokerAddrSet);
    }


    @Transactional(rollbackFor = Exception.class)
    public int update(Topic topic) {
        try {
            int affected = topicRepository.update(topic);
            createAndUpdateTopicConfig(topic);
            return affected;
        } catch (Exception e) {
            throw new ServiceException(500, "Update topic failed: " + e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int delete(BigInteger id) {
        var optionalTopic = topicRepository.findById(id);
        if (optionalTopic.isEmpty()) {
            throw new ResourceNotFoundException();
        }
        try {
            int affected = topicRepository.deleteById(id);

            ClusterInfo clusterInfo = mqAdminService.execute(DefaultMQAdminExt::examineBrokerClusterInfo);
            List<String> brokerAddrList = resolveBrokerAddrs(clusterInfo, null);

            if (!brokerAddrList.isEmpty()) {
                mqAdminService.executeVoid(admin -> {
                    for (String brokerAddr : brokerAddrList) {
                        admin.deleteTopic(optionalTopic.get().getTopicName(), brokerAddr);
                    }
                });
            }

            return affected;
        } catch (Exception e) {
            throw new ServiceException(500, "Delete topic failed: " + e.getMessage());
        }
    }

    public TopicRouteData route(String topic) {
        try {
            return mqAdminService.execute(admin -> admin.examineTopicRouteInfo(topic));
        } catch (Exception e) {
            throw new ServiceException(500, "Query topic route failed: " + e.getMessage());
        }
    }


    public TopicConfig examineTopicConfig(String topic, String brokerName) {
        try {
            ClusterInfo clusterInfo = mqAdminService.execute(DefaultMQAdminExt::examineBrokerClusterInfo);
            if (clusterInfo == null || clusterInfo.getBrokerAddrTable() == null) {
                throw new ServiceException(500, "Cluster info is empty");
            }
            BrokerData brokerData = clusterInfo.getBrokerAddrTable().get(brokerName);
            if (brokerData == null) {
                throw new ServiceException(404, "Broker not found: " + brokerName);
            }
            String brokerAddr = brokerData.selectBrokerAddr();
            if (StringUtils.isBlank(brokerAddr)) {
                throw new ServiceException(500, "Broker address is empty: " + brokerName);
            }
            return mqAdminService.execute(admin -> admin.examineTopicConfig(brokerAddr, topic));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(500, "Query topic config failed: " + e.getMessage());
        }
    }

    public List<TopicConfigInfo> examineTopicConfig(String topic) {
        List<TopicConfigInfo> topicConfigInfoList = new ArrayList<>();
        TopicRouteData topicRouteData = route(topic);
        if (topicRouteData == null || topicRouteData.getBrokerDatas() == null) {
            return topicConfigInfoList;
        }
        for (BrokerData brokerData : topicRouteData.getBrokerDatas()) {
            if (brokerData == null || StringUtils.isBlank(brokerData.getBrokerName())) {
                continue;
            }
            TopicConfigInfo topicConfigInfo = new TopicConfigInfo();
            TopicConfig topicConfig = examineTopicConfig(topic, brokerData.getBrokerName());
            BeanUtils.copyProperties(topicConfig, topicConfigInfo);
            topicConfigInfo.setBrokerNameList(Collections.singletonList(brokerData.getBrokerName()));
            String messageType = topicConfig.getAttributes() == null
                    ? null
                    : topicConfig.getAttributes().get("message.type");
            if (StringUtils.isBlank(messageType)) {
                messageType = TopicMessageType.UNSPECIFIED.name();
            }
            topicConfigInfo.setMessageType(MessageType.valueOf(messageType));
            topicConfigInfoList.add(topicConfigInfo);
        }
        return topicConfigInfoList;
    }

    public TopicStatsTable stats(String topic) throws Exception {
        return mqAdminService.execute(admin -> admin.examineTopicStats(topic));

    }
    public void createOrUpdate(TopicConfigInfo topicCreateOrUpdateRequest) throws Exception {
        Topic topic  = topicRepository.findByTopicName(topicCreateOrUpdateRequest.getTopicName());


        if (topic != null) {
            topicRepository.updateNonNull(topic);
        } else {
            topic = new Topic();
            topic.setTopicName(topicCreateOrUpdateRequest.getTopicName());
            topic.setDescription(topicCreateOrUpdateRequest.getTopicName());
            topic.setSystemTopic(!"NORMAL".equals(topicCreateOrUpdateRequest.getMessageType().getValue()));
            topic.setWriteQueueNums(topicCreateOrUpdateRequest.getWriteQueueNums());
            topic.setReadQueueNums(topicCreateOrUpdateRequest.getReadQueueNums());
            topic.setMessageType(topicCreateOrUpdateRequest.getMessageType());
            topicRepository.insert(topic);
        }
        createAndUpdateTopicConfig(topic, topicCreateOrUpdateRequest.getBrokerNameList());
    }


    public Topic findByTopicName(String topicName) {
        return topicRepository.findByTopicName(topicName);
    }

    public SendResult sendTopicMessage(SendTopicMessageCommand sendTopicMessageCommand) {
        if (sendTopicMessageCommand == null) {
            throw new ServiceException(400, "Send command is required");
        }

        if (StringUtils.isBlank(sendTopicMessageCommand.getTopic())) {
            throw new ServiceException(400, "Topic is required");
        }

        if (StringUtils.isBlank(sendTopicMessageCommand.getMessageBody())) {
            throw new ServiceException(400, "Message body is required");
        }

        try {
            Message message = new Message(
                    sendTopicMessageCommand.getTopic(),
                    sendTopicMessageCommand.getMessageBody().getBytes(StandardCharsets.UTF_8)
            );

            if (StringUtils.isNotBlank(sendTopicMessageCommand.getTag())) {
                message.setTags(sendTopicMessageCommand.getTag());
            }

            if (StringUtils.isNotBlank(sendTopicMessageCommand.getKey())) {
                message.setKeys(sendTopicMessageCommand.getKey());
            }

            return mqProducerService.send(message);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(500, "Send topic message failed: " + e.getMessage());
        }
    }

    public GroupList queryTopicConsumerInfoByTopicName(String topic) throws Exception {
        return mqAdminService.execute(admin -> admin.queryTopicConsumeByWho(topic));
    }
}