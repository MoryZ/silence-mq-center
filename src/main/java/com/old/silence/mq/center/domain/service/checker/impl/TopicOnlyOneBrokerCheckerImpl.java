
package com.old.silence.mq.center.domain.service.checker.impl;

import org.springframework.stereotype.Service;
import com.old.silence.mq.center.domain.service.checker.CheckerType;
import com.old.silence.mq.center.domain.service.checker.RocketMqChecker;


/**
 * TODO
 * here the checkers is not implemented yet
 */
@Service
public class TopicOnlyOneBrokerCheckerImpl implements RocketMqChecker {
    @Override
    public Object doCheck() {
        return null;
    }

    @Override
    public CheckerType checkerType() {
        return CheckerType.TOPIC_ONLY_ONE_BROKER_CHECK;
    }
}
