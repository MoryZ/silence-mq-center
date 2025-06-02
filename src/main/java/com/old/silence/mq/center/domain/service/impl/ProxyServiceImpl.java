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
package com.old.silence.mq.center.domain.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.google.common.collect.Maps;
import com.old.silence.mq.center.api.config.RMQConfigure;
import com.old.silence.mq.center.domain.service.ProxyService;
import com.old.silence.mq.center.domain.service.client.ProxyAdmin;

import java.util.List;
import java.util.Map;

@Service
public class ProxyServiceImpl implements ProxyService {

    private static final Logger log = LoggerFactory.getLogger(ProxyServiceImpl.class);
    protected final ProxyAdmin proxyAdmin;
    private final RMQConfigure configure;

    public ProxyServiceImpl(ProxyAdmin proxyAdmin, RMQConfigure configure) {
        this.proxyAdmin = proxyAdmin;
        this.configure = configure;
    }

    @Override
    public void addProxyAddrList(String proxyAddr) {
        List<String> proxyAddrs = configure.getProxyAddrs();
        if (proxyAddrs != null && !proxyAddrs.contains(proxyAddr)) {
            proxyAddrs.add(proxyAddr);
        }
        configure.setProxyAddrs(proxyAddrs);
    }

    @Override
    public void updateProxyAddrList(String proxyAddr) {
        configure.setProxyAddr(proxyAddr);
    }

    @Override
    public Map<String, Object> getProxyHomePage() {
        Map<String, Object> homePageInfoMap = Maps.newHashMap();
        homePageInfoMap.put("currentProxyAddr", configure.getProxyAddr());
        homePageInfoMap.put("proxyAddrList", configure.getProxyAddrs());
        return homePageInfoMap;
    }
}
