package com.old.silence.mq.center.aspect;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.old.silence.mq.center.domain.service.client.MQAdminInstance;

@Aspect
@Service
public class MQAdminAspect {


    private static final Logger log = LoggerFactory.getLogger(MQAdminAspect.class);
    private final GenericObjectPool<MQAdminExt> mqAdminExtPool;

    public MQAdminAspect(GenericObjectPool<MQAdminExt> mqAdminExtPool) {
        this.mqAdminExtPool = mqAdminExtPool;
    }

    @Pointcut("execution(* com.old.silence.mq.center.domain.service.client.MQAdminExtImpl..*(..))")
    public void mQAdminMethodPointCut() {

    }

    @Around(value = "mQAdminMethodPointCut()")
    public Object aroundMQAdminMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object obj;
        try {
            MQAdminInstance.createMQAdmin(mqAdminExtPool);
            obj = joinPoint.proceed();
        } finally {
            MQAdminInstance.returnMQAdmin(mqAdminExtPool);
            log.debug("op=look method={} cost={}", joinPoint.getSignature().getName(), System.currentTimeMillis() - start);
        }
        return obj;
    }
}
