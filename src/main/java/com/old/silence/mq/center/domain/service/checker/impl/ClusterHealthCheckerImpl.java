package com.old.silence.mq.center.domain.service.checker.impl;

import org.springframework.stereotype.Service;
import com.old.silence.mq.center.domain.service.checker.CheckerType;
import com.old.silence.mq.center.domain.service.checker.RocketMqChecker;

@Service
public class ClusterHealthCheckerImpl implements RocketMqChecker {
    @Override
    public Object doCheck() {
        return null;
    }

    @Override
    public CheckerType checkerType() {
        return CheckerType.CLUSTER_HEALTH_CHECK;
    }
}
