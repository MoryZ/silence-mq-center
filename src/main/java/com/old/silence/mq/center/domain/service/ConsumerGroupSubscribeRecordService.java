package com.old.silence.mq.center.domain.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.mq.center.domain.model.ConsumerGroupSubscribeRecord;
import com.old.silence.mq.center.domain.repository.ConsumerGroupSubscribeRecordRepository;
import com.old.silence.mq.center.exception.ServiceException;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author moryzang
 */
@Service
public class ConsumerGroupSubscribeRecordService {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerGroupSubscribeRecordService.class);

    private final ConsumerGroupSubscribeRecordRepository consumerGroupSubscribeRecordRepository;

    public ConsumerGroupSubscribeRecordService(ConsumerGroupSubscribeRecordRepository consumerGroupSubscribeRecordRepository) {
        this.consumerGroupSubscribeRecordRepository = consumerGroupSubscribeRecordRepository;
    }

    public List<String> findGroupNamesByTopicId(BigInteger topicId) {
        try {

            return consumerGroupSubscribeRecordRepository.findByQuery(
                            new LambdaQueryWrapper<ConsumerGroupSubscribeRecord>()
                                    .eq(ConsumerGroupSubscribeRecord::getTopicId, topicId)
                    ).stream()
                    .map(ConsumerGroupSubscribeRecord::getGroupName)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to query subscribe records by topicId: {}", topicId, e);
            throw new ServiceException(500, "查询订阅组记录失败");
        }
    }
}
