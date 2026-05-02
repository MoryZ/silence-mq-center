package com.old.silence.mq.center.api;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.old.silence.mq.center.domain.service.MessageService;
import com.old.silence.mq.center.domain.service.MessageTraceService;
import com.old.silence.mq.center.dto.MessageTraceGraph;
import com.old.silence.mq.center.vo.MessageTraceView;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/messageTrace")
public class MessageTraceController {

    private final MessageService messageService;

    private final MessageTraceService messageTraceService;

    public MessageTraceController(MessageService messageService, MessageTraceService messageTraceService) {
        this.messageService = messageService;
        this.messageTraceService = messageTraceService;
    }

    @GetMapping(value = "/viewMessage")
    public Map<String, Object> viewMessage(@RequestParam(required = false) String topic, @RequestParam String msgId) throws Exception {
        return messageService.viewMessage(topic, msgId);
    }

    @GetMapping(value = "/viewMessageTraceDetail")
    public List<MessageTraceView> viewTraceMessages(@RequestParam(required = false) String topic, @RequestParam String msgId) throws Exception {
        if (StringUtils.isBlank(topic)) {
            topic = "RMQ_SYS_TRACE_TOPIC"; // 硬编码
        }

        return messageTraceService.queryMessageTraceKey(msgId, topic);
    }

    @GetMapping(value = "/viewMessageTraceGraph")
    public MessageTraceGraph viewMessageTraceGraph(@RequestParam String msgId,
                                                   @RequestParam(required = false) String topic) throws Exception {
        return messageTraceService.queryMessageTraceGraph(msgId, topic);
    }
}
