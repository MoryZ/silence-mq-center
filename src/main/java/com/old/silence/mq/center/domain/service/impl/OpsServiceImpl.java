
package com.old.silence.mq.center.domain.service.impl;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.springframework.stereotype.Service;
import com.google.common.collect.Maps;
import com.old.silence.mq.center.api.config.RMQConfigure;
import com.old.silence.mq.center.domain.service.AbstractCommonService;
import com.old.silence.mq.center.domain.service.OpsService;
import com.old.silence.mq.center.domain.service.checker.CheckerType;
import com.old.silence.mq.center.domain.service.checker.RocketMqChecker;

import java.util.List;
import java.util.Map;

@Service
public class OpsServiceImpl extends AbstractCommonService implements OpsService {

    private final RMQConfigure configure;

    private final GenericObjectPool<MQAdminExt> mqAdminExtPool;

    private final List<RocketMqChecker> rocketMqCheckerList;

    protected OpsServiceImpl(MQAdminExt mqAdminExt, RMQConfigure configure, GenericObjectPool<MQAdminExt> mqAdminExtPool, List<RocketMqChecker> rocketMqCheckerList) {
        super(mqAdminExt);
        this.configure = configure;
        this.mqAdminExtPool = mqAdminExtPool;
        this.rocketMqCheckerList = rocketMqCheckerList;
    }


    @Override
    public Map<String, Object> homePageInfo() {
        Map<String, Object> homePageInfoMap = Maps.newHashMap();
        homePageInfoMap.put("currentNamesrv", configure.getNamesrvAddr());
        homePageInfoMap.put("namesvrAddrList", configure.getNamesrvAddrs());
        homePageInfoMap.put("useVIPChannel", Boolean.valueOf(configure.getIsVIPChannel()));
        homePageInfoMap.put("useTLS", configure.isUseTLS());
        return homePageInfoMap;
    }

    @Override
    public void updateNameSvrAddrList(String nameSvrAddrList) {
        configure.setNamesrvAddr(nameSvrAddrList);
        // when update namesrvAddr, clean the mqAdminExt objects pool.
        mqAdminExtPool.clear();
    }

    @Override
    public String getNameSvrList() {
        return configure.getNamesrvAddr();
    }

    @Override
    public Map<CheckerType, Object> rocketMqStatusCheck() {
        Map<CheckerType, Object> checkResultMap = Maps.newHashMap();
        for (RocketMqChecker rocketMqChecker : rocketMqCheckerList) {
            checkResultMap.put(rocketMqChecker.checkerType(), rocketMqChecker.doCheck());
        }
        return checkResultMap;
    }

    @Override
    public boolean updateIsVIPChannel(String useVIPChannel) {
        configure.setIsVIPChannel(useVIPChannel);
        mqAdminExtPool.clear();
        return true;
    }

    @Override
    public boolean updateUseTLS(boolean useTLS) {
        configure.setUseTLS(useTLS);
        mqAdminExtPool.clear();
        return true;
    }

    @Override
    public void addNameSvrAddr(String namesrvAddr) {
        List<String> namesrvAddrs = configure.getNamesrvAddrs();
        if (namesrvAddrs != null && !namesrvAddrs.contains(namesrvAddr)) {
            namesrvAddrs.add(namesrvAddr);
        }
        configure.setNamesrvAddrs(namesrvAddrs);
    }
}
