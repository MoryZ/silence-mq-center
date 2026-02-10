package com.old.silence.mq.center.domain.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.old.silence.mq.center.api.config.RMQConfigure;
import com.old.silence.mq.center.domain.service.DashboardCollectService;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DashboardCollectServiceImpl implements DashboardCollectService {

    private final static Logger log = LoggerFactory.getLogger(DashboardCollectServiceImpl.class);
    private final RMQConfigure configure;
    private final LoadingCache<String, List<String>> brokerMap = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .concurrencyLevel(10)
            .recordStats()
            .ticker(Ticker.systemTicker())
            .removalListener(new RemovalListener<Object, Object>() {
                @Override
                public void onRemoval(RemovalNotification<Object, Object> notification) {
                    log.debug(notification.getKey() + " was removed, cause is " + notification.getCause());
                }
            })
            .build(
                    new CacheLoader<>() {
                        @Override
                        public List<String> load(String key) {
                            return Collections.emptyList();
                        }
                    }
            );

    private final LoadingCache<String, List<String>> topicMap = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .concurrencyLevel(10)
            .recordStats()
            .ticker(Ticker.systemTicker())
            .removalListener(new RemovalListener<Object, Object>() {
                @Override
                public void onRemoval(RemovalNotification<Object, Object> notification) {
                    log.debug(notification.getKey() + " was removed, cause is " + notification.getCause());
                }
            })
            .build(
                    new CacheLoader<>() {
                        @Override
                        public List<String> load(String key) {
                            return Lists.newArrayList();
                        }
                    }
            );

    public DashboardCollectServiceImpl(RMQConfigure configure) {
        this.configure = configure;
    }

    @Override
    public LoadingCache<String, List<String>> getBrokerMap() {
        return brokerMap;
    }

    @Override
    public LoadingCache<String, List<String>> getTopicMap() {
        return topicMap;
    }

    @Override
    public Map<String, List<String>> jsonDataFile2map(File file) {
        List<String> strings;
        try {
            strings = Files.readLines(file, Charsets.UTF_8);
        } catch (IOException e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
        StringBuffer sb = new StringBuffer();
        for (String string : strings) {
            sb.append(string);
        }
        JSONObject json = (JSONObject) JSONObject.parse(sb.toString());
        Set<Map.Entry<String, Object>> entries = json.entrySet();
        Map<String, List<String>> map = Maps.newHashMap();
        for (Map.Entry<String, Object> entry : entries) {
            JSONArray tpsArray = (JSONArray) entry;
            if (tpsArray == null) {
                continue;
            }
            Object[] tpsStrArray = tpsArray.toArray();
            List<String> tpsList = Lists.newArrayList();
            for (Object tpsObj : tpsStrArray) {
                tpsList.add("" + tpsObj);
            }
            map.put(entry.getKey(), tpsList);
        }
        return map;
    }

    @Override
    public Map<String, List<String>> getBrokerCache(String date) {
        String dataLocationPath = configure.getDashboardCollectData();
        File file = new File(dataLocationPath + date + ".json");
        if (!file.exists()) {
            log.info("No dashboard data for broker cache data: {}", date);
            return Maps.newHashMap();
        }
        return jsonDataFile2map(file);
    }

    @Override
    public Map<String, List<String>> getTopicCache(String date) {
        String dataLocationPath = configure.getDashboardCollectData();
        File file = new File(dataLocationPath + date + "_topic" + ".json");
        if (!file.exists()) {
            log.info("No dashboard data for data: {}", date);
            //throw Throwables.propagate(new ServiceException(1, "This date have't data!"));
            return Maps.newHashMap();
        }
        return jsonDataFile2map(file);
    }

}
