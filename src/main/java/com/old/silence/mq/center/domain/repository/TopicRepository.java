package com.old.silence.mq.center.domain.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.old.silence.mq.center.domain.model.permission.Topic;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * Topic Repository
 */
@Mapper
public interface TopicRepository extends BaseMapper<Topic> {

    /**
     * 根据 Topic 名称查找
     */
    Optional<Topic> findByTopicName(String topicName);

    /**
     * 查询指定集群下的所有 Topic
     */
    @Select("SELECT t FROM topic t WHERE t.clusterName = :clusterName AND t.status = 'ACTIVE'")
    List<Topic> findByClusterName(@Param("clusterName") String clusterName);

    /**
     * 查询指定所有者的所有 Topic
     */
    @Select("SELECT t FROM topic t WHERE t.ownerId = :ownerId AND t.status = 'ACTIVE'")
    List<Topic> findByOwnerId(@Param("ownerId") BigInteger ownerId);

    /**
     * 查询所有非系统 Topic
     */
    @Select("SELECT t FROM topic t WHERE t.isSystemTopic = 0 AND t.status = 'ACTIVE'")
    List<Topic> findAllUserTopics();

    /**
     * 检查 Topic 是否存在
     */
    boolean existsByTopicName(String topicName);
}
