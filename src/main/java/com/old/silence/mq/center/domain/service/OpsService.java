
package com.old.silence.mq.center.domain.service;

import com.old.silence.mq.center.domain.service.checker.CheckerType;

import java.util.Map;

public interface OpsService {
    Map<String, Object> homePageInfo();

    void updateNameSvrAddrList(String nameSvrAddrList);

    String getNameSvrList();

    Map<CheckerType, Object> rocketMqStatusCheck();

    boolean updateIsVIPChannel(String useVIPChannel);

    boolean updateUseTLS(boolean useTLS);

    void addNameSvrAddr(String namesrvAddr);
}
