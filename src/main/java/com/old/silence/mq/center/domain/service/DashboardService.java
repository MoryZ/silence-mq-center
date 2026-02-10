package com.old.silence.mq.center.domain.service;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    /**
     * @param date format yyyy-MM-dd
     */
    Map<String, List<String>> queryBrokerData(String date);

    /**
     * @param date format yyyy-MM-dd
     */
    Map<String, List<String>> queryTopicData(String date);

    /**
     * @param date      format yyyy-MM-dd
     * @param topicName 111
     */
    List<String> queryTopicData(String date, String topicName);

    List<String> queryTopicCurrentData();

}
