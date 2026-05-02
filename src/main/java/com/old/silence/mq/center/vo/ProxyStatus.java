package com.old.silence.mq.center.vo;

/**
 * @author moryzang
 */
public class ProxyStatus {

    private String addr;      // Proxy 地址 (IP:Port)
    private boolean alive;   // 是否存活
    private long lastCheck;  // 最后检查时间戳

    public ProxyStatus(String addr, boolean alive) {
        this.addr = addr;
        this.alive = alive;
        this.lastCheck = System.currentTimeMillis();
    }

    // 标准的 Getter 和 Setter
    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public long getLastCheck() {
        return lastCheck;
    }

    public void setLastCheck(long lastCheck) {
        this.lastCheck = lastCheck;
    }
}
