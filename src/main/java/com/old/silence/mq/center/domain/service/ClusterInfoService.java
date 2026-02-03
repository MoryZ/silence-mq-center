
package com.old.silence.mq.center.domain.service;

import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.old.silence.mq.center.domain.service.facade.RocketMQClientFacade;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ClusterInfoService {

    private static final Logger log = LoggerFactory.getLogger(ClusterInfoService.class);
    private final MQAdminExt mqAdminExt;
    private final RocketMQClientFacade mqFacade;

    @Value("${rocketmq.cluster.cache.expire:60000}")
    private long cacheExpireMs;


    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicReference<ClusterInfo> cachedRef = new AtomicReference<>();

    public ClusterInfoService(MQAdminExt mqAdminExt, RocketMQClientFacade mqFacade) {
        this.mqAdminExt = mqAdminExt;
        this.mqFacade = mqFacade;
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
            // 🎯 使用 Facade 获取集群信息，自动处理异常和转换
            // Facade 返回清晰的 ClusterInfoDTO，但这里仍保留对 ClusterInfo 的支持
            ClusterInfo fresh = mqAdminExt.examineBrokerClusterInfo();
            cachedRef.set(fresh);
            log.info("op=refreshClusterInfo success");
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
