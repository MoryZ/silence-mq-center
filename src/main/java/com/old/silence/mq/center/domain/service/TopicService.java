package com.old.silence.mq.center.domain.service;

import java.math.BigInteger;

import org.apache.rocketmq.common.TopicConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.core.exception.ResourceNotFoundException;
import com.old.silence.mq.center.exception.ServiceException;
import com.old.silence.mq.center.domain.model.Topic;
import com.old.silence.mq.center.domain.repository.TopicRepository;

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
}