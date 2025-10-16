

package com.old.silence.mq.center.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
@ConfigurationProperties(prefix = "thread-pool.config")
public class CollectExecutorConfig {
    private int coreSize = 20;
    private int maxSize = 20;
    private long keepAliveTime = 3000L;
    private int queueSize = 1000;

    @Bean(name = "collectExecutor")
    public ExecutorService collectExecutor(CollectExecutorConfig collectExecutorConfig) {
        return new ThreadPoolExecutor(
            collectExecutorConfig.getCoreSize(),
            collectExecutorConfig.getMaxSize(),
            collectExecutorConfig.getKeepAliveTime(),
            TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<>(collectExecutorConfig.getQueueSize()),
            new ThreadFactory() {
                private final AtomicLong threadIndex = new AtomicLong(0);

                @Override
                public Thread newThread(@NonNull Runnable r) {
                    return new Thread(r, "collectTopicThread_" + this.threadIndex.incrementAndGet());
                }
            },
            new ThreadPoolExecutor.DiscardOldestPolicy()
        );
    }

    public int getCoreSize() {
        return coreSize;
    }

    public void setCoreSize(int coreSize) {
        this.coreSize = coreSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }
}
