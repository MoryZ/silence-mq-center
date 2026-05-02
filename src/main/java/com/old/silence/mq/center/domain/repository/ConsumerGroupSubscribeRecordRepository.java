package com.old.silence.mq.center.domain.repository;

import com.old.silence.data.mybatis.projection.ProjectionMapperRepository;
import com.old.silence.mq.center.domain.model.ConsumerGroupSubscribeRecord;

import java.math.BigInteger;

public interface ConsumerGroupSubscribeRecordRepository extends ProjectionMapperRepository<ConsumerGroupSubscribeRecord, BigInteger> {
}