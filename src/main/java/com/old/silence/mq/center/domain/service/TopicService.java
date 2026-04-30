package com.old.silence.mq.center.domain.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.attribute.TopicMessageType;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
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
import com.old.silence.mq.center.exception.ServiceException;
import com.old.silence.mq.center.domain.model.Topic;
import com.old.silence.mq.center.domain.repository.TopicRepository;
import com.old.silence.mq.center.vo.TopicConfigInfo;

@Service
public class TopicService {

    private final MQAdminService mqAdminService;
    private final TopicRepository topicRepository;

    public TopicService(MQAdminService mqAdminService, TopicRepository topicRepository) {
        this.mqAdminService = mqAdminService;
        this.topicRepository = topicRepository;
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
        mqAdminService.executeVoid(admin -> {
            TopicConfig topicConfig = new TopicConfig(topic.getTopicName());
            topicConfig.setReadQueueNums(topic.getReadQueueNums());
            topicConfig.setWriteQueueNums(topic.getWriteQueueNums());
            // RocketMQ 5.3.1 必须设置消息类型
            topicConfig.getAttributes().put("+message.type", topic.getMessageType());
            admin.createAndUpdateTopicConfig(topic.getBrokerAddr(), topicConfig);
        });
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
            mqAdminService.executeVoid(admin ->
                admin.deleteTopic(optionalTopic.get().getTopicName(), optionalTopic.get().getBrokerAddr())
            );
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
                : topicConfig.getAttributes().get("+message.type");
            if (StringUtils.isBlank(messageType)) {
                messageType = TopicMessageType.UNSPECIFIED.name();
            }
            topicConfigInfo.setMessageType(messageType);
            topicConfigInfoList.add(topicConfigInfo);
        }
        return topicConfigInfoList;
    }

    public TopicStatsTable stats(String topic) throws Exception {
        return mqAdminService.execute(admin -> admin.examineTopicStats(topic));

    }

    public void createOrUpdate(TopicConfigInfo topicCreateOrUpdateRequest) throws Exception {

        var existsByTopicName = topicRepository.existsByTopicName(topicCreateOrUpdateRequest.getTopicName());


        Topic topic = new Topic();
        topic.setTopicName(topicCreateOrUpdateRequest.getTopicName());
        topic.setBrokerAddr(topicCreateOrUpdateRequest.getBrokerNameList().get(0));
        topic.setClusterName(topicCreateOrUpdateRequest.getClusterNameList().get(0));
        topic.setDescription(topicCreateOrUpdateRequest.getTopicName());
        topic.setSystemTopic(!"NORMAL".equals(topicCreateOrUpdateRequest.getMessageType()));

        topic.setMessageType(topicCreateOrUpdateRequest.getMessageType());
        topic.setOwnerId(BigInteger.ONE);

        if (existsByTopicName) {
            topicRepository.update(topic);
        } else {
            topicRepository.insert(topic);
        }
        createAndUpdateTopicConfig(topic);
    }

    public Topic findByTopicName(String topicName) {
        return topicRepository.findByTopicName(topicName);
    }
}