package com.old.silence.mq.center.api;

import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.tools.admin.api.MessageTrack;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.google.common.collect.Maps;
import com.old.silence.mq.center.domain.model.MessageTraceView;
import com.old.silence.mq.center.domain.model.MessageView;
import com.old.silence.mq.center.domain.model.trace.MessageTraceGraph;
import com.old.silence.mq.center.domain.service.MessageService;
import com.old.silence.mq.center.domain.service.MessageTraceService;

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
    public Map<String, Object> viewMessage(@RequestParam(required = false) String topic, @RequestParam String msgId) {
        Map<String, Object> messageViewMap = Maps.newHashMap();
        Pair<MessageView, List<MessageTrack>> messageViewListPair = messageService.viewMessage(topic, msgId);
        messageViewMap.put("messageView", messageViewListPair.getObject1());
        return  messageViewMap;
    }

    @GetMapping(value = "/viewMessageTraceDetail")
    public List<MessageTraceView> viewTraceMessages(@RequestParam String msgId) {
        return  messageTraceService.queryMessageTraceKey(msgId);
    }

    @GetMapping(value = "/viewMessageTraceGraph")
    public MessageTraceGraph viewMessageTraceGraph(@RequestParam String msgId,
                                                   @RequestParam(required = false) String traceTopic) {
        return  messageTraceService.queryMessageTraceGraph(msgId, traceTopic);
    }
}
