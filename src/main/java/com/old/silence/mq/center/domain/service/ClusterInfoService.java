
package com.old.silence.mq.center.domain.service;

import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ClusterInfoService {

    private static final Logger log = LoggerFactory.getLogger(ClusterInfoService.class);
    private final MQAdminExt mqAdminExt;

    @Value("${rocketmq.cluster.cache.expire:60000}")
    private long cacheExpireMs;


    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicReference<ClusterInfo> cachedRef = new AtomicReference<>();

    public ClusterInfoService(MQAdminExt mqAdminExt) {
        this.mqAdminExt = mqAdminExt;
    }


    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(this::refresh,
                0, cacheExpireMs / 2, TimeUnit.MILLISECONDS);
    }

    public ClusterInfo get() {
        ClusterInfo info = cachedRef.get();
        return info != null ? info : refresh();
    }

    public synchronized ClusterInfo refresh() {
        try {
            ClusterInfo fresh = mqAdminExt.examineBrokerClusterInfo();
            cachedRef.set(fresh);
            return fresh;
        } catch (Exception e) {
            log.warn("Refresh cluster info failed", e);
            ClusterInfo old = cachedRef.get();
            if (old != null) {
                return old;
            }
            throw new IllegalStateException("No cluster info available", e);
        }
    }
}
