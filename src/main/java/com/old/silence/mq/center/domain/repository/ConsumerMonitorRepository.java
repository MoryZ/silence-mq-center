package com.old.silence.mq.center.domain.repository;

import com.old.silence.data.mybatis.projection.ProjectionMapperRepository;
import com.old.silence.mq.center.domain.model.ConsumerMonitor;

import java.math.BigInteger;

public interface ConsumerMonitorRepository extends ProjectionMapperRepository<ConsumerMonitor, BigInteger> {
}