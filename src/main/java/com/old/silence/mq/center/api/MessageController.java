/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.old.silence.mq.center.api;

import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.remoting.protocol.body.ConsumeMessageDirectlyResult;
import org.apache.rocketmq.tools.admin.api.MessageTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.google.common.collect.Maps;
import com.old.silence.mq.center.domain.model.MessagePage;
import com.old.silence.mq.center.domain.model.MessageView;
import com.old.silence.mq.center.domain.model.request.MessageQuery;
import com.old.silence.mq.center.domain.service.MessageService;
import com.old.silence.mq.center.util.JsonUtil;

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
    public Map<String, Object> viewMessage(@RequestParam(required = false) String topic, @RequestParam String msgId) {
        Map<String, Object> messageViewMap = Maps.newHashMap();
        Pair<MessageView, List<MessageTrack>> messageViewListPair = messageService.viewMessage(topic, msgId);
        messageViewMap.put("messageView", messageViewListPair.getObject1());
        messageViewMap.put("messageTrackList", messageViewListPair.getObject2());
        return  messageViewMap;
    }

    @GetMapping("/queryMessagePageByTopic")
    public MessagePage queryMessagePageByTopic(MessageQuery query) {
        return  messageService.queryMessageByPage(query);
    }

    @GetMapping(value = "/queryMessageByTopicAndKey")
    public List<MessageView> queryMessageByTopicAndKey(@RequestParam String topic, @RequestParam String key) {
        return  messageService.queryMessageByTopicAndKey(topic, key);
    }

    @GetMapping(value = "/queryMessageByTopic")
    public List<MessageView> queryMessageByTopic(@RequestParam String topic, @RequestParam long begin,
                                      @RequestParam long end) {
        return  messageService.queryMessageByTopic(topic, begin, end);
    }

    @PostMapping(value = "/consumeMessageDirectly")
    public ConsumeMessageDirectlyResult consumeMessageDirectly(@RequestParam String topic, @RequestParam String consumerGroup,
                                         @RequestParam String msgId,
                                         @RequestParam(required = false) String clientId) {
        logger.info("msgId={} consumerGroup={} clientId={}", msgId, consumerGroup, clientId);
        ConsumeMessageDirectlyResult consumeMessageDirectlyResult = messageService.consumeMessageDirectly(topic, msgId, consumerGroup, clientId);
        logger.info("consumeMessageDirectlyResult={}", JsonUtil.obj2String(consumeMessageDirectlyResult));
        return consumeMessageDirectlyResult;
    }
}
