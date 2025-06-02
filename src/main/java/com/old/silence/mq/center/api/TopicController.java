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

import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.body.GroupList;
import org.apache.rocketmq.remoting.protocol.body.TopicList;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.google.common.base.Preconditions;
import com.old.silence.mq.center.domain.model.TopicConsumerInfo;
import com.old.silence.mq.center.domain.model.request.SendTopicMessageRequest;
import com.old.silence.mq.center.domain.model.request.TopicConfigInfo;
import com.old.silence.mq.center.domain.model.request.TopicTypeList;
import com.old.silence.mq.center.domain.service.ConsumerService;
import com.old.silence.mq.center.domain.service.TopicService;
import com.old.silence.mq.center.util.JsonUtil;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class TopicController {
    private final Logger logger = LoggerFactory.getLogger(TopicController.class);

    private final TopicService topicService;

    private final ConsumerService consumerService;

    public TopicController(TopicService topicService, ConsumerService consumerService) {
        this.topicService = topicService;
        this.consumerService = consumerService;
    }

    @GetMapping(value = "/topics")
    public TopicList list(@RequestParam(required = false) boolean skipSysProcess,
                                     @RequestParam(required = false) boolean skipRetryAndDlq) {
        return  topicService.fetchAllTopicList(skipSysProcess, skipRetryAndDlq);
    }

    @PostMapping(value = "/topics/refresh")
    public Boolean refresh() {
        return  topicService.refreshTopicList();
    }

    @GetMapping(value = "/topics/topicType")
    public TopicTypeList listTopicType() {
        return  topicService.examineAllTopicType();
    }

    @GetMapping(value = "/topics/stats")
    public TopicStatsTable stats(@RequestParam String topic) {
        return  topicService.stats(topic);
    }

    @GetMapping(value = "/topics/routes")
    public TopicRouteData route(@RequestParam String topic) {
        return  topicService.route(topic);
    }


    @PostMapping(value = "/topics/createOrUpdate")
    public Boolean topicCreateOrUpdateRequest(@RequestBody TopicConfigInfo topicCreateOrUpdateRequest) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(topicCreateOrUpdateRequest.getBrokerNameList())
                        || CollectionUtils.isNotEmpty(topicCreateOrUpdateRequest.getClusterNameList()),
            "clusterName or brokerName can not be all blank");
        logger.info("op=look topicCreateOrUpdateRequest:{}", JsonUtil.obj2String(topicCreateOrUpdateRequest));
        topicService.createOrUpdate(topicCreateOrUpdateRequest);
        return  true;
    }

    @GetMapping(value = "/topics/queryConsumerByTopic")
    public Map<String, TopicConsumerInfo> queryConsumerByTopic(@RequestParam String topic) {
        return  consumerService.queryConsumeStatsListByTopicName(topic);
    }

    @RequestMapping(value = "/topics/queryTopicConsumerInfo")
    public GroupList queryTopicConsumerInfo(@RequestParam String topic) {
        return  topicService.queryTopicConsumerInfo(topic);
    }

    @GetMapping(value = "/topics/examineTopicConfig")
    public List<TopicConfigInfo> examineTopicConfig(@RequestParam String topic) {
        return  topicService.examineTopicConfig(topic);
    }

    @PostMapping(value = "/topics/send")
    public SendResult sendTopicMessage(
        @RequestBody SendTopicMessageRequest sendTopicMessageRequest) throws RemotingException, MQClientException, InterruptedException {
        return  topicService.sendTopicMessageRequest(sendTopicMessageRequest);
    }

    @DeleteMapping(value = "/topics/delete")
    public Boolean delete(@RequestParam(required = false) String clusterName, @RequestParam String topic) {
        return  topicService.deleteTopic(topic, clusterName);
    }

    @PostMapping(value = "/topics/deleteTopicByBroker")
    public Boolean deleteTopicByBroker(@RequestParam String brokerName, @RequestParam String topic) {
        return  topicService.deleteTopicInBroker(brokerName, topic);
    }

}
