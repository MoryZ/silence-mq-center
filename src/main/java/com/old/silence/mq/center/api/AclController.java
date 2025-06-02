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
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.AclConfig;
import org.apache.rocketmq.common.PlainAccessConfig;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.google.common.base.Preconditions;
import com.old.silence.mq.center.api.config.RMQConfigure;
import com.old.silence.mq.center.domain.model.request.AclRequest;
import com.old.silence.mq.center.domain.service.AclService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/acl")
public class AclController {

    private final AclService aclService;

    private final RMQConfigure configure;

    public AclController(AclService aclService, RMQConfigure configure) {
        this.aclService = aclService;
        this.configure = configure;
    }

    @GetMapping("/enable")
    public Boolean isEnableAcl() {
        return  configure.isACLEnabled();
    }

    @GetMapping("/config")
    public AclConfig getAclConfig() {
        return  aclService.getAclConfig(false);
    }

    @PostMapping("/add")
    public Boolean addAclConfig(@RequestBody PlainAccessConfig config) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(config.getAccessKey()), "accessKey is null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(config.getSecretKey()), "secretKey is null");
        aclService.addAclConfig(config);
        return true;
    }

    @DeleteMapping("/delete")
    public Boolean deleteAclConfig(@RequestBody PlainAccessConfig config) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(config.getAccessKey()), "accessKey is null");
        aclService.deleteAclConfig(config);
        return  true;
    }

    @PostMapping("/update")
    public Boolean updateAclConfig(@RequestBody PlainAccessConfig config) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(config.getSecretKey()), "secretKey is null");
        aclService.updateAclConfig(config);
        return  true;
    }

    @PostMapping("/topic/add")
    public Boolean addAclTopicConfig(@RequestBody AclRequest request) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(request.getConfig().getAccessKey()), "accessKey is null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(request.getConfig().getSecretKey()), "secretKey is null");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(request.getConfig().getTopicPerms()), "topic perms is null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(request.getTopicPerm()), "topic perm is null");
        aclService.addOrUpdateAclTopicConfig(request);
        return true;
    }

    @PostMapping("/group/add")
    public Boolean addAclGroupConfig(@RequestBody AclRequest request) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(request.getConfig().getAccessKey()), "accessKey is null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(request.getConfig().getSecretKey()), "secretKey is null");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(request.getConfig().getGroupPerms()), "group perms is null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(request.getGroupPerm()), "group perm is null");
        aclService.addOrUpdateAclGroupConfig(request);
        return  true;
    }

    @PostMapping("/perm/delete")
    public Boolean deletePermConfig(@RequestBody AclRequest request) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(request.getConfig().getAccessKey()), "accessKey is null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(request.getConfig().getSecretKey()), "secretKey is null");
        aclService.deletePermConfig(request);
        return  true;
    }

    @PostMapping("/sync.")
    public Boolean syncConfig(@RequestBody PlainAccessConfig config) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(config.getAccessKey()), "accessKey is null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(config.getSecretKey()), "secretKey is null");
        aclService.syncData(config);
        return  true;
    }

    @PostMapping("/white/list/add")
    public Boolean addWhiteList(@RequestBody List<String> whiteList) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(whiteList), "white list is null");
        aclService.addWhiteList(whiteList);
        return  true;
    }

    @DeleteMapping("/white/list/delete")
    public Boolean deleteWhiteAddr(@RequestParam String request) {
        aclService.deleteWhiteAddr(request);
        return  true;
    }

    @PostMapping("/white/list/sync")
    public Boolean synchronizeWhiteList(@RequestBody List<String> whiteList) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(whiteList), "white list is null");
        aclService.synchronizeWhiteList(whiteList);
        return  true;
    }
}
