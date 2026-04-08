package com.old.silence.mq.center.domain.repository;

import java.math.BigInteger;

import org.apache.ibatis.annotations.Mapper;

import com.old.silence.data.mybatis.projection.ProjectionMapperRepository;
import com.old.silence.mq.center.domain.model.ConsumerMonitor;

public interface ConsumerMonitorRepository extends ProjectionMapperRepository<ConsumerMonitor, BigInteger> {
}