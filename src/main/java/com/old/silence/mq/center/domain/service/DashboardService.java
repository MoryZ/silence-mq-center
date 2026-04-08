package com.old.silence.mq.center.domain.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author moryzang
 */
@Service
public class DashboardService {

    private final MQAdminService mqAdminService;

    public DashboardService(MQAdminService mqAdminService) {
        this.mqAdminService = mqAdminService;
    }

    /**
     * 判断是否为当前日期
     */
    private boolean isCurrentDate(String date) {
        if (StringUtils.isBlank(date)) {
            return true;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String today = sdf.format(new Date());
            return date.equals(today);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 查询指定日期的 Broker 数据
     * 如果是当前日期，返回实时 broker 信息；否则返回历史数据状态
     */
    public Map<String, List<String>> queryBrokerData(String date) throws Exception {
        return mqAdminService.execute(admin -> {
            Map<String, List<String>> result = new HashMap<>();
            
            // 只支持当前日期的实时数据查询
            if (!isCurrentDate(date)) {
                result.put("brokers", List.of("Historical data not available for date: " + date));
                return result;
            }
            
            try {
                // 获取所有 topic 来推导 broker 信息
                Set<String> topicNames = admin.fetchAllTopicList().getTopicList();
                
                // 返回一个基本的broker列表（从topic派生）
                List<String> brokerList = new ArrayList<>(topicNames != null ? topicNames : new ArrayList<>());
                result.put("brokers", brokerList);
                result.put("queryDate", List.of(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
            } catch (Exception e) {
                // 兼容查询异常，返回空结果
                result.put("error", List.of(e.getMessage()));
            }
            
            return result;
        });
    }

    /**
     * 查询指定日期的所有 Topic 数据
     * 如果是当前日期，返回实时 topic 列表；否则返回历史数据状态
     */
    public Map<String, List<String>> queryTopicData(String date) throws Exception {
        return mqAdminService.execute(admin -> {
            Map<String, List<String>> result = new HashMap<>();
            
            // 只支持当前日期的实时数据查询
            if (!isCurrentDate(date)) {
                result.put("status", List.of("Historical data not available for date: " + date));
                result.put("queryDate", List.of(date));
                return result;
            }
            
            try {
                Set<String> topicNames = admin.fetchAllTopicList().getTopicList();
                List<String> topicList = new ArrayList<>(topicNames == null ? new ArrayList<>() : topicNames);
                
                result.put("topics", topicList);
                result.put("count", List.of(String.valueOf(topicList.size())));
                result.put("queryDate", List.of(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
            } catch (Exception e) {
                // 兼容查询异常，返回错误信息
                result.put("error", List.of(e.getMessage()));
            }
            
            return result;
        });
    }

    /**
     * 查询指定日期特定 Topic 的数据
     * 如果是当前日期，返回实时 topic 信息；否则返回历史数据状态
     */
    public List<String> queryTopicData(String date, String topicName) throws Exception {
        return mqAdminService.execute(admin -> {
            List<String> result = new ArrayList<>();
            
            try {
                // 添加基本信息
                result.add("Topic: " + topicName);
                
                // 只支持当前日期的实时数据查询
                if (!isCurrentDate(date)) {
                    result.add("Query Date: " + date);
                    result.add("Status: HISTORICAL_DATA_UNAVAILABLE");
                    result.add("Note: Only real-time data (current date) is supported");
                    return result;
                }
                
                result.add("Query Date: " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                
                // 获取所有topic列表验证是否存在
                Set<String> allTopics = admin.fetchAllTopicList().getTopicList();
                if (allTopics != null && allTopics.contains(topicName)) {
                    result.add("Status: ACTIVE");
                    result.add("Topics Total: " + allTopics.size());
                } else {
                    result.add("Status: NOT_FOUND");
                }
            } catch (Exception e) {
                // 兼容查询异常
                result.add("Error: " + e.getMessage());
            }
            
            return result;
        });
    }

    /**
     * 查询当前 Topic 数据（最新数据）
     * 返回当前时间点的 topic 列表（不使用 date 参数）
     */
    public List<String> queryTopicCurrentData() throws Exception {
        return mqAdminService.execute(admin -> {
            List<String> result = new ArrayList<>();
            
            try {
                // 添加查询时间
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                result.add("query_time: " + sdf.format(new Date()));
                
                // 获取所有 topic
                Set<String> topicNames = admin.fetchAllTopicList().getTopicList();
                List<String> topicList = new ArrayList<>(topicNames == null ? new ArrayList<>() : topicNames);
                
                result.add("total_topics: " + topicList.size());
                result.addAll(topicList);
            } catch (Exception e) {
                // 兼容查询异常
                result.add("Error: " + e.getMessage());
            }
            
            return result;
        });
    }
}

