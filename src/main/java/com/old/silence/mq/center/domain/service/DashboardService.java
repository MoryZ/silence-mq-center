package com.old.silence.mq.center.domain.service;

import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author moryzang
 */
@Service
public class DashboardService {

    private final DashboardCollectService dashboardCollectService;

    public DashboardService(DashboardCollectService dashboardCollectService) {
        this.dashboardCollectService = dashboardCollectService;
    }


    /**
     * @param date format yyyy-MM-dd
     */
    public Map<String, Object> queryBrokerData(String date) {
        Map<String, List<String>> brokerSeries = safeSeriesMap(dashboardCollectService.getBrokerCache(date));
        return buildDashboardPayload(date, "brokers", "brokerRealtime", brokerSeries);
    }

    public Map<String, Object> queryTopicData(String date) {
        Map<String, List<String>> topicSeries = safeSeriesMap(dashboardCollectService.getTopicCache(date));
        return buildDashboardPayload(date, "topics", "topicRealtime", topicSeries);
    }

    /**
     * @param date format yyyy-MM-dd
     * @param topicName 111
     */
    public Map<String, Object> queryTopicData(String date, String topicName) {
        Map<String, List<String>> cache = safeSeriesMap(dashboardCollectService.getTopicCache(date));
        List<String> topicSeries = cache.getOrDefault(topicName, new ArrayList<>());
        long realtime = topicSeries.isEmpty() ? 0L : extractRealtimeValue(topicSeries.get(topicSeries.size() - 1));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("queryDate", Collections.singletonList(date));
        result.put("topicName", topicName);
        result.put("realtime", realtime);
        result.put("series", topicSeries);
        return result;
    }

    public Map<String, Object> queryTopicCurrentData() {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, List<String>> topicCache = safeSeriesMap(dashboardCollectService.getTopicCache(format.format(date)));

        if (topicCache.isEmpty()) {
            Map<String, Object> emptyResult = new LinkedHashMap<>();
            emptyResult.put("queryTime", "");
            emptyResult.put("totalTopics", 0);
            emptyResult.put("topics", new ArrayList<>());
            emptyResult.put("topicRealtime", new LinkedHashMap<>());
            return emptyResult;
        }

        Map<String, Long> topicRealtime = buildRealtimeMap(topicCache);
        List<String> sortedTopics = topicRealtime.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("queryTime", dateTimeFormat.format(date));
        result.put("totalTopics", sortedTopics.size());
        result.put("topics", sortedTopics);
        result.put("topicRealtime", topicRealtime);
        return result;
    }

    private Map<String, List<String>> safeSeriesMap(Map<String, List<String>> raw) {
        return raw == null ? new LinkedHashMap<>() : raw;
    }

    private Map<String, Object> buildDashboardPayload(
            String date,
            String rankListKey,
            String realtimeMapKey,
            Map<String, List<String>> series
    ) {
        Map<String, Long> realtime = buildRealtimeMap(series);
        List<String> rankedNames = realtime.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("queryDate", Collections.singletonList(date));
        result.put(rankListKey, rankedNames);
        result.put(realtimeMapKey, realtime);
        result.put("series", series);
        return result;
    }

    private Map<String, Long> buildRealtimeMap(Map<String, List<String>> source) {
        Map<String, Long> result = new LinkedHashMap<>();
        if (source == null || source.isEmpty()) {
            return result;
        }

        for (Map.Entry<String, List<String>> entry : source.entrySet()) {
            List<String> points = entry.getValue();
            if (points == null || points.isEmpty()) {
                result.put(entry.getKey(), 0L);
                continue;
            }

            String latestPoint = points.get(points.size() - 1);
            result.put(entry.getKey(), extractRealtimeValue(latestPoint));
        }
        return result;
    }

    private long extractRealtimeValue(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return 0L;
        }

        String[] parts = raw.split(",");
        if (parts.length > 4) {
            return safeParseLong(parts[4]);
        }

        if (parts.length > 1) {
            return safeParseLong(parts[1]);
        }

        return 0L;
    }

    private long safeParseLong(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (Exception ignored) {
            return 0L;
        }
    }
}

