package com.old.silence.mq.center.domain.service;

import com.google.common.cache.LoadingCache;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface DashboardCollectService {
    // todo just move the task to org.apache.rocketmq.dashboard.task.DashboardCollectTask
    // the code can be reconstruct
    LoadingCache<String, List<String>> getBrokerMap();

    LoadingCache<String, List<String>> getTopicMap();

    Map<String, List<String>> jsonDataFile2map(File file);

    Map<String, List<String>> getBrokerCache(String date);

    Map<String, List<String>> getTopicCache(String date);
}
