package com.old.silence.mq.center.domain.service;

import java.util.Map;

public interface ProxyService {

    void addProxyAddrList(String proxyAddr);

    void updateProxyAddrList(String proxyAddr);

    Map<String, Object> getProxyHomePage();
}
