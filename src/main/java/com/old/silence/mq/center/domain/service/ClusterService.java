

package com.old.silence.mq.center.domain.service;

import java.util.Map;
import java.util.Properties;

public interface ClusterService {
    Map<String, Object> list();

    Properties getBrokerConfig(String brokerAddr);
}
