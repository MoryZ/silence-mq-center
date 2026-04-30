package com.old.silence.mq.center.domain.repository;

import java.math.BigInteger;

import com.old.silence.data.mybatis.projection.ProjectionMapperRepository;
import com.old.silence.mq.center.domain.model.ConsumerGroupSubscribeRecord;

public interface ConsumerGroupSubscribeRecordRepository extends ProjectionMapperRepository<ConsumerGroupSubscribeRecord, BigInteger> {
}