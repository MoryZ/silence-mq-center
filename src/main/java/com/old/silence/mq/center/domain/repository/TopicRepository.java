package com.old.silence.mq.center.domain.repository;

import org.apache.ibatis.annotations.Select;
import com.old.silence.data.mybatis.projection.ProjectionMapperRepository;
import com.old.silence.mq.center.domain.model.Topic;

import java.math.BigInteger;

/**
 * Topic Repository
 */
public interface TopicRepository extends ProjectionMapperRepository<Topic, BigInteger> {


    /**
     * 检查 Topic 是否存在
     */
    @Select("SELECT count(1) FROM rmq_topic WHERE topic_name = #{topicName}")
    boolean existsByTopicName(String topicName);

    @Select("SELECT * FROM rmq_topic WHERE topic_name = #{topicName}")
    Topic findByTopicName(String topicName);
}
