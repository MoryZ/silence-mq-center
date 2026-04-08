package com.old.silence.mq.center.api;

import org.apache.rocketmq.remoting.protocol.body.ConsumeMessageDirectlyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.json.JacksonMapper;
import com.old.silence.mq.center.dto.MessagePage;
import com.old.silence.mq.center.dto.MessageView;
import com.old.silence.mq.center.dto.MessageQuery;
import com.old.silence.mq.center.domain.service.MessageService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/message")
public class MessageController {
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping(value = "/viewMessage")
    public Map<String, Object> viewMessage(@RequestParam String topic, @RequestParam String msgId) throws Exception {
        return messageService.viewMessage(topic, msgId);
    }

    @GetMapping("/queryMessagePageByTopic")
    public MessagePage queryMessagePageByTopic(MessageQuery query) {
        return messageService.queryMessageByPage(query);
    }

    @GetMapping(value = "/queryMessageByTopicAndKey")
    public List<MessageView> queryMessageByTopicAndKey(@RequestParam String topic, @RequestParam String key) throws Exception {
        return messageService.queryMessageByTopicAndKey(topic, key);
    }

    @GetMapping(value = "/queryMessageByTopic")
    public List<MessageView> queryMessageByTopic(@RequestParam String topic, @RequestParam long begin,
                                                 @RequestParam long end) throws Exception {
        return messageService.queryMessageByTopic(topic, begin, end);
    }

    @PostMapping(value = "/consumeMessageDirectly")
    public ConsumeMessageDirectlyResult consumeMessageDirectly(@RequestParam String topic, @RequestParam String consumerGroup,
                                                               @RequestParam String msgId,
                                                               @RequestParam(required = false) String clientId) throws Exception {
        logger.info("msgId={} consumerGroup={} clientId={}", msgId, consumerGroup, clientId);
        ConsumeMessageDirectlyResult consumeMessageDirectlyResult = messageService.consumeMessageDirectly(topic, msgId, consumerGroup, clientId);
        logger.info("consumeMessageDirectlyResult={}", JacksonMapper.getSharedInstance().toJson(consumeMessageDirectlyResult));
        return consumeMessageDirectlyResult;
    }
}
