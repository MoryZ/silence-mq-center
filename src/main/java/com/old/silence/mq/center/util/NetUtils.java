package com.old.silence.mq.center.util;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author moryzang
 */
public final class NetUtils {

    private NetUtils() {
        throw new AssertionError();
    }

    /**
     * 检测 Proxy 端口是否可达
     *
     * @param addr 格式为 "127.0.0.1:8081"
     * @return 是否连通
     */
    public static boolean isAddressReachable(String addr) {
        try {
            String[] parts = addr.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            try (Socket socket = new Socket()) {
                // 设置 2 秒超时，防止后端线程被挂死
                socket.connect(new InetSocketAddress(host, port), 2000);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
