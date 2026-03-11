

package com.old.silence.mq.center.domain.service.impl;


import org.springframework.stereotype.Service;
import com.old.silence.mq.center.domain.service.DashboardCollectService;
import com.old.silence.mq.center.domain.service.DashboardService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final DashboardCollectService dashboardCollectService;

    public DashboardServiceImpl(DashboardCollectService dashboardCollectService) {
        this.dashboardCollectService = dashboardCollectService;
    }

    /**
     * @param date format yyyy-MM-dd
     */
    @Override
    public Map<String, List<String>> queryBrokerData(String date) {
        return dashboardCollectService.getBrokerCache(date);
    }

    @Override
    public Map<String, List<String>> queryTopicData(String date) {
        return dashboardCollectService.getTopicCache(date);
    }

    /**
     * @param date format yyyy-MM-dd
     * @param topicName 111
     */
    @Override
    public List<String> queryTopicData(String date, String topicName) {
        org.apache.commons.lang3.Preconditions.checkArgument(
            org.apache.commons.lang3.StringUtils.isNotEmpty(date),
            "date must not be empty"
        );
        org.apache.commons.lang3.Preconditions.checkArgument(
            org.apache.commons.lang3.StringUtils.isNotEmpty(topicName),
            "topicName must not be empty"
        );
        Map<String, List<String>> cache = dashboardCollectService.getTopicCache(date);
        if (cache != null) {
            return cache.get(topicName);  // 可能返回 null，但这是合理的
        }
        return null;  // 合理的返回值，调用方应检查
    }

    @Override
    public List<String> queryTopicCurrentData() {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, List<String>> topicCache = dashboardCollectService.getTopicCache(format.format(date));
        // ✅ 防守性检查：cache 可能为 null
        if (topicCache == null || topicCache.isEmpty()) {
            return new ArrayList<>();  // 返回空列表而不是 null
        }
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : topicCache.entrySet()) {
            List<String> value = entry.getValue();
            // ✅ 防守性检查：value 可能为 null 或空
            if (value != null && !value.isEmpty()) {
                String[] data = value.get(value.size() - 1).split(",");
                if (data.length > 4) {
                    result.add(entry.getKey() + "," + data[4]);
                }
            }
        }
        return result;
    }
}
