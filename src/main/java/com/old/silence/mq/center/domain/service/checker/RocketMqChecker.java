package com.old.silence.mq.center.domain.service.checker;

public interface RocketMqChecker {
    Object doCheck();

    CheckerType checkerType();

}
