package com.old.silence.mq.center.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.data.commons.converter.QueryWrapperConverter;
import com.old.silence.json.JacksonMapper;
import com.old.silence.mq.center.api.assembler.TopicMapper;
import com.old.silence.mq.center.domain.model.Topic;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigInteger;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.old.silence.mq.center.domain.service.TopicService;
import com.old.silence.mq.center.dto.TopicCommand;
import com.old.silence.mq.center.dto.TopicPageQuery;
import com.old.silence.mq.center.vo.TopicConfigInfo;

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
    private final TopicMapper topicMapper;
    private final JacksonMapper jacksonMapper;

    public TopicController(TopicService topicService, TopicMapper topicMapper, JacksonMapper jacksonMapper) {
        this.topicService = topicService;
        this.topicMapper = topicMapper;
        this.jacksonMapper = jacksonMapper;
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
        return  topicService.stats(topic);
    }

    @GetMapping(value = "/topics/examineTopicConfig")
    public List<TopicConfigInfo> examineTopicConfig(@RequestParam String topic) {
        return  topicService.examineTopicConfig(topic);
    }

    @PostMapping(value = "/topics/createOrUpdate")
    public Boolean topicCreateOrUpdateRequest(@RequestBody TopicConfigInfo topicCreateOrUpdateRequest) throws Exception {
        log.info("op=look topicCreateOrUpdateRequest:{}", jacksonMapper.toJson(topicCreateOrUpdateRequest));
        topicService.createOrUpdate(topicCreateOrUpdateRequest);
        return true;
    }

}
