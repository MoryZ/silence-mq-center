package com.old.silence.mq.center.domain.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.body.KVTable;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Maps;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author moryzang
 */
@Service
public class DashboardCollectService {
    private static final Logger log = LoggerFactory.getLogger(DashboardCollectService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int MAX_POINTS_PER_SERIES = 720;

    private final MQAdminService mqAdminService;

    private final Map<String, Map<String, List<String>>> brokerHistoryByDate = new ConcurrentHashMap<>();
    private final Map<String, Map<String, List<String>>> topicHistoryByDate = new ConcurrentHashMap<>();

    private final LoadingCache<String, Map<String, List<String>>> brokerCache = CacheBuilder.newBuilder()
            .maximumSize(64)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .removalListener(new RemovalListener<Object, Object>() {
                @Override
                public void onRemoval(RemovalNotification<Object, Object> notification) {
                    log.debug("dashboard broker cache expired, key={}, cause={}", notification.getKey(), notification.getCause());
                }
            })
            .build(new CacheLoader<>() {
                @Override
                public Map<String, List<String>> load(String key) {
                    return buildBrokerSeriesSnapshot(key);
                }
            });

    private final LoadingCache<String, Map<String, List<String>>> topicCache = CacheBuilder.newBuilder()
            .maximumSize(64)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .removalListener(new RemovalListener<Object, Object>() {
                @Override
                public void onRemoval(RemovalNotification<Object, Object> notification) {
                    log.debug("dashboard topic cache expired, key={}, cause={}", notification.getKey(), notification.getCause());
                }
            })
            .build(new CacheLoader<>() {
                @Override
                public Map<String, List<String>> load(String key) {
                    return buildTopicSeriesSnapshot(key);
                }
            });

    public DashboardCollectService(MQAdminService mqAdminService) {
        this.mqAdminService = mqAdminService;
    }

    public Map<String, List<String>> getBrokerCache(String date) {
        try {
            return deepCopySeriesMap(brokerCache.get(date));
        } catch (Exception e) {
            log.warn("Load broker dashboard cache failed for date={}", date, e);
            return Maps.newHashMap();
        }
    }

    public Map<String, List<String>> getTopicCache(String date) {
        try {
            return deepCopySeriesMap(topicCache.get(date));
        } catch (Exception e) {
            log.warn("Load topic dashboard cache failed for date={}", date, e);
            return Maps.newHashMap();
        }
    }

    private Map<String, List<String>> buildBrokerSeriesSnapshot(String date) {
        Map<String, List<String>> current = deepCopySeriesMap(brokerHistoryByDate.getOrDefault(date, new LinkedHashMap<>()));
        if (!isToday(date)) {
            return current;
        }

        Map<String, Long> realtime = collectBrokerRealtime();
        appendDataPoints(current, realtime);
        brokerHistoryByDate.put(date, current);
        return current;
    }

    private Map<String, List<String>> buildTopicSeriesSnapshot(String date) {
        Map<String, List<String>> current = deepCopySeriesMap(topicHistoryByDate.getOrDefault(date, new LinkedHashMap<>()));
        if (!isToday(date)) {
            return current;
        }

        Map<String, Long> realtime = collectTopicRealtime();
        appendDataPoints(current, realtime);
        topicHistoryByDate.put(date, current);
        return current;
    }

    private Map<String, Long> collectBrokerRealtime() {
        try {
            return mqAdminService.execute(admin -> {
                Map<String, Long> result = new LinkedHashMap<>();
                ClusterInfo clusterInfo = admin.examineBrokerClusterInfo();
                if (clusterInfo == null || clusterInfo.getBrokerAddrTable() == null) {
                    return result;
                }

                for (Map.Entry<String, BrokerData> entry : clusterInfo.getBrokerAddrTable().entrySet()) {
                    String brokerName = entry.getKey();
                    BrokerData brokerData = entry.getValue();
                    if (brokerData == null) {
                        continue;
                    }

                    String brokerAddr = brokerData.selectBrokerAddr();
                    if (StringUtils.isBlank(brokerAddr) && brokerData.getBrokerAddrs() != null) {
                        brokerAddr = brokerData.getBrokerAddrs().get(0L);
                    }
                    if (StringUtils.isBlank(brokerAddr)) {
                        continue;
                    }

                    try {
                        KVTable runtimeStats = admin.fetchBrokerRuntimeStats(brokerAddr);
                        Map<String, String> table = runtimeStats == null ? null : runtimeStats.getTable();
                        long realtime = pickLong(table,
                                "msgPutTotalToday",
                                "putMessageTimesTotal",
                                "msgPutTotalYesterday",
                                "putMessageSizeTotal");
                        result.put(brokerName, realtime);
                    } catch (Exception ex) {
                        log.debug("Collect broker runtime failed, broker={}, addr={}", brokerName, brokerAddr, ex);
                    }
                }
                return result;
            });
        } catch (Exception e) {
            log.warn("Collect broker realtime metrics failed", e);
            return new LinkedHashMap<>();
        }
    }

    private Map<String, Long> collectTopicRealtime() {
        try {
            return mqAdminService.execute(admin -> {
                Map<String, Long> result = new LinkedHashMap<>();
                Set<String> topics = admin.fetchAllTopicList().getTopicList();
                if (topics == null || topics.isEmpty()) {
                    return result;
                }

                for (String topic : topics.stream().filter(StringUtils::isNotBlank).sorted().collect(Collectors.toList())) {
                    try {
                        TopicRouteData topicRoute = admin.examineTopicRouteInfo(topic);
                        if (topicRoute == null || topicRoute.getQueueDatas() == null || topicRoute.getQueueDatas().isEmpty()) {
                            result.put(topic, 0L);
                            continue;
                        }

                        long total = 0L;
                        int queueId = 0;
                        for (var queueData : topicRoute.getQueueDatas()) {
                            if (queueData == null || StringUtils.isBlank(queueData.getBrokerName())) {
                                queueId++;
                                continue;
                            }
                            MessageQueue mq = new MessageQueue(topic, queueData.getBrokerName(), queueId);
                            total += admin.maxOffset(mq);
                            queueId++;
                        }
                        result.put(topic, total);
                    } catch (Exception ex) {
                        log.debug("Collect topic realtime failed, topic={}", topic, ex);
                    }
                }
                return result;
            });
        } catch (Exception e) {
            log.warn("Collect topic realtime metrics failed", e);
            return new LinkedHashMap<>();
        }
    }

    private void appendDataPoints(Map<String, List<String>> seriesMap, Map<String, Long> realtime) {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : realtime.entrySet()) {
            String name = entry.getKey();
            long value = entry.getValue() == null ? 0L : entry.getValue();
            List<String> points = seriesMap.computeIfAbsent(name, key -> new java.util.ArrayList<>());
            points.add(formatPoint(now, value));
            if (points.size() > MAX_POINTS_PER_SERIES) {
                points.remove(0);
            }
        }
    }

    private String formatPoint(long timestamp, long value) {
        return timestamp + ",0,0,0," + value;
    }

    private boolean isToday(String date) {
        return LocalDate.now().format(DATE_FORMATTER).equals(date);
    }

    private long pickLong(Map<String, String> table, String... keys) {
        if (table == null || table.isEmpty() || keys == null) {
            return 0L;
        }
        for (String key : keys) {
            String raw = table.get(key);
            if (StringUtils.isBlank(raw)) {
                continue;
            }
            try {
                return Long.parseLong(raw.trim());
            } catch (Exception ignored) {
                // keep trying other candidate keys
            }
        }
        return 0L;
    }

    private Map<String, List<String>> deepCopySeriesMap(Map<String, List<String>> source) {
        Map<String, List<String>> copy = new LinkedHashMap<>();
        if (source == null || source.isEmpty()) {
            return copy;
        }

        for (Map.Entry<String, List<String>> entry : source.entrySet()) {
            List<String> points = entry.getValue();
            copy.put(entry.getKey(), points == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(points));
        }
        return copy;
    }
}
