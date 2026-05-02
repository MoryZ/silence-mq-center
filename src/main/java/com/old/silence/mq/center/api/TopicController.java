package com.old.silence.mq.center.api;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.body.GroupList;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.data.commons.converter.QueryWrapperConverter;
import com.old.silence.json.JacksonMapper;
import com.old.silence.mq.center.api.assembler.TopicMapper;
import com.old.silence.mq.center.domain.model.Topic;
import com.old.silence.mq.center.domain.service.ConsumerGroupSubscribeRecordService;
import com.old.silence.mq.center.domain.service.ConsumerService;
import com.old.silence.mq.center.domain.service.TopicService;
import com.old.silence.mq.center.dto.GroupConsumeInfo;
import com.old.silence.mq.center.dto.SendTopicMessageCommand;
import com.old.silence.mq.center.dto.TopicCommand;
import com.old.silence.mq.center.dto.TopicConsumerInfo;
import com.old.silence.mq.center.dto.TopicConsumerInfoDetail;
import com.old.silence.mq.center.dto.TopicPageQuery;
import com.old.silence.mq.center.vo.TopicConfigInfo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.old.silence.webmvc.util.RestControllerUtils.validateModifyingResult;

/**
 * 基础功能 查询 集群 系统topic 查询,创建
 * 表格列 Topic 所属系统 所属集群 创建时间
 * 操作列 路由状态 信息维护 删除
 *
 */
@RestController
@RequestMapping("/api/v1")
public class TopicController {


    private static final Logger log = LoggerFactory.getLogger(TopicController.class);
    private final TopicService topicService;
    private final ConsumerService consumerService;
    private final ConsumerGroupSubscribeRecordService consumerGroupSubscribeRecordService;
    private final JacksonMapper jacksonMapper;
    private final TopicMapper topicMapper;

    public TopicController(TopicService topicService,
                           ConsumerService consumerService,
                           ConsumerGroupSubscribeRecordService consumerGroupSubscribeRecordService,
                           JacksonMapper jacksonMapper,
                           TopicMapper topicMapper) {
        this.topicService = topicService;
        this.consumerService = consumerService;
        this.consumerGroupSubscribeRecordService = consumerGroupSubscribeRecordService;
        this.jacksonMapper = jacksonMapper;
        this.topicMapper = topicMapper;
    }

    @GetMapping(path = "/topics", params = {"pageNo", "pageSize"})
    public IPage<Topic> queryTopicPage(TopicPageQuery query,
                                       Page<Topic> page) {
        QueryWrapper<Topic> queryWrapper = QueryWrapperConverter.convert(query, Topic.class);
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = query.getKeyword();
            queryWrapper.and(w -> w.lambda()
                    .eq(Topic::getTopicName, keyword)
                    .or()
                    .eq(Topic::getDescription, keyword));
        }
        return topicService.queryTopicPage(queryWrapper, page);
    }

    @PostMapping("/topics")
    public BigInteger create(@RequestBody TopicCommand command) {
        var topic = topicMapper.convert(command);
        return topicService.create(topic);
    }

    @PutMapping("/topics/{id}")
    public void update(@PathVariable BigInteger id, @RequestBody TopicCommand command) {
        var topic = topicMapper.convert(command);
        topic.setId(id); // NO SONAR
        validateModifyingResult(topicService.update(topic));
    }

    @DeleteMapping("/topics/{id}")
    public void delete(@PathVariable BigInteger id) {
        validateModifyingResult(topicService.delete(id));
    }


    // 以下是非必要接口

    @GetMapping(value = "/topics/stats")
    public TopicStatsTable stats(@RequestParam String topic) throws Exception {
        return topicService.stats(topic);
    }

    @GetMapping(value = "/topics/examineTopicConfig")
    public List<TopicConfigInfo> examineTopicConfig(@RequestParam String topic) {
        return topicService.examineTopicConfig(topic);
    }

    @GetMapping(value = "/topics/routes")
    public TopicRouteData route(@RequestParam String topic) {
        return topicService.route(topic);
    }

    @PostMapping(value = "/topics/send")
    public SendResult sendTopicMessage(@RequestBody SendTopicMessageCommand sendTopicMessageCommand) {
        return topicService.sendTopicMessage(sendTopicMessageCommand);
    }

    @GetMapping(value = "/topics/queryTopicConsumerInfo")
    public GroupList queryTopicConsumerInfo(@RequestParam String topic) throws Exception {
        return topicService.queryTopicConsumerInfoByTopicName(topic);
    }

    @GetMapping(value = "/topics/queryConsumerByTopic")
    public List<TopicConsumerInfoDetail> queryConsumerByTopic(@RequestParam String topicName) {
        var topic = topicService.findByTopicName(topicName);
        if (topic == null || topic.getId() == null) {
            return null;
        }

        var groupNames = consumerGroupSubscribeRecordService.findGroupNamesByTopicId(topic.getId());
        List<TopicConsumerInfoDetail> topicConsumerInfoDetails = new ArrayList<>();
        for (var groupName : groupNames) {
            List<TopicConsumerInfo> topicConsumerInfos = null;
            try {
                topicConsumerInfos = consumerService.queryTopicConsumerInfo(topicName, groupName);
            } catch (Exception ignored) {

            }
            var topicConsumerInfo = CollectionUtils.isEmpty(topicConsumerInfos) ? new TopicConsumerInfo(topicName) : topicConsumerInfos.get(0);
            topicConsumerInfoDetails.add(new TopicConsumerInfoDetail(groupName, topicConsumerInfo));
        }
        return topicConsumerInfoDetails;
    }

    @PostMapping(value = "/topics/createOrUpdate")
    public Boolean topicCreateOrUpdateRequest(@RequestBody TopicConfigInfo topicCreateOrUpdateRequest) throws Exception {
        log.info("op=look topicCreateOrUpdateRequest:{}", jacksonMapper.toJson(topicCreateOrUpdateRequest));
        topicService.createOrUpdate(topicCreateOrUpdateRequest);
        return true;
    }

}
