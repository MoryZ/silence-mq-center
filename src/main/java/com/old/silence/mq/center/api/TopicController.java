package com.old.silence.mq.center.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.data.commons.converter.QueryWrapperConverter;
import com.old.silence.mq.center.api.assembler.TopicMapper;
import com.old.silence.mq.center.domain.model.Topic;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigInteger;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.old.silence.mq.center.domain.service.TopicService;
import com.old.silence.mq.center.dto.TopicCommand;
import com.old.silence.mq.center.dto.TopicPageQuery;
import static com.old.silence.webmvc.util.RestControllerUtils.validateModifyingResult;

@RestController
@RequestMapping("/api/v1")
public class TopicController {

    private final TopicService topicService;
    private final TopicMapper topicMapper;

    public TopicController(TopicService topicService, TopicMapper topicMapper) {
        this.topicService = topicService;
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

}
