package com.old.silence.mq.center.domain.model;

public class ConsumerMonitorConfig {
    private int minCount;
    private int maxDiffTotal;

    public ConsumerMonitorConfig() {
    }

    public ConsumerMonitorConfig(int minCount, int maxDiffTotal) {
        this.minCount = minCount;
        this.maxDiffTotal = maxDiffTotal;
    }

    public int getMinCount() {
        return minCount;
    }

    public void setMinCount(int minCount) {
        this.minCount = minCount;
    }

    public int getMaxDiffTotal() {
        return maxDiffTotal;
    }

    public void setMaxDiffTotal(int maxDiffTotal) {
        this.maxDiffTotal = maxDiffTotal;
    }
}
