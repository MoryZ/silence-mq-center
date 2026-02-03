package com.old.silence.mq.center.domain.repository;

import com.old.silence.mq.center.domain.model.permission.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Topic Repository
 */
@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    /**
     * 根据 Topic 名称查找
     */
    Optional<Topic> findByTopicName(String topicName);

    /**
     * 查询指定集群下的所有 Topic
     */
    @Query("SELECT t FROM Topic t WHERE t.clusterName = :clusterName AND t.status = 'ACTIVE'")
    List<Topic> findByClusterName(@Param("clusterName") String clusterName);

    /**
     * 查询指定所有者的所有 Topic
     */
    @Query("SELECT t FROM Topic t WHERE t.ownerId = :ownerId AND t.status = 'ACTIVE'")
    List<Topic> findByOwnerId(@Param("ownerId") Long ownerId);

    /**
     * 查询所有非系统 Topic
     */
    @Query("SELECT t FROM Topic t WHERE t.isSystemTopic = 0 AND t.status = 'ACTIVE'")
    List<Topic> findAllUserTopics();

    /**
     * 检查 Topic 是否存在
     */
    boolean existsByTopicName(String topicName);
}
