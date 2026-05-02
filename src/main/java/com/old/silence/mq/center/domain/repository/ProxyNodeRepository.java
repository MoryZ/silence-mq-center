package com.old.silence.mq.center.domain.repository;


import com.old.silence.data.mybatis.projection.ProjectionMapperRepository;
import com.old.silence.mq.center.domain.model.ProxyNode;

import java.math.BigInteger;

/**
 * Topic Repository
 */
public interface ProxyNodeRepository extends ProjectionMapperRepository<ProxyNode, BigInteger> {


}
