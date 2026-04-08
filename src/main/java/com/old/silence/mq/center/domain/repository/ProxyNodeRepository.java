package com.old.silence.mq.center.domain.repository;


import java.math.BigInteger;

import com.old.silence.data.mybatis.projection.ProjectionMapperRepository;
import com.old.silence.mq.center.domain.model.ProxyNode;

/**
 * Topic Repository
 */
public interface ProxyNodeRepository extends ProjectionMapperRepository<ProxyNode, BigInteger> {


}
