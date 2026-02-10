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
import com.old.silence.mq.center.domain.service.facade.RocketMQClientFacade;
import com.old.silence.mq.center.domain.service.helper.ConfigManagementHelper;

import java.util.List;
import java.util.Map;

@Service
public class OpsServiceImpl extends AbstractCommonService implements OpsService {

    private final RMQConfigure configure;

    private final GenericObjectPool<MQAdminExt> mqAdminExtPool;

    private final List<RocketMqChecker> rocketMqCheckerList;

    private final RocketMQClientFacade mqFacade;

    protected OpsServiceImpl(MQAdminExt mqAdminExt, RMQConfigure configure, GenericObjectPool<MQAdminExt> mqAdminExtPool, List<RocketMqChecker> rocketMqCheckerList, RocketMQClientFacade mqFacade) {
        super(mqAdminExt);
        this.configure = configure;
        this.mqAdminExtPool = mqAdminExtPool;
        this.rocketMqCheckerList = rocketMqCheckerList;
        this.mqFacade = mqFacade;
    }


    @Override
    public Map<String, Object> homePageInfo() {
        Map<String, Object> infoMap = ConfigManagementHelper.buildInfoMap();
        ConfigManagementHelper.putConfigToMap(infoMap, "currentNamesrv", configure.getNamesrvAddr());
        ConfigManagementHelper.putConfigToMap(infoMap, "namesvrAddrList", configure.getNamesrvAddrs());
        ConfigManagementHelper.putConfigToMap(infoMap, "useVIPChannel", Boolean.valueOf(configure.getIsVIPChannel()));
        ConfigManagementHelper.putConfigToMap(infoMap, "useTLS", configure.isUseTLS());
        return infoMap;
    }

    @Override
    public void updateNameSvrAddrList(String nameSvrAddrList) {
        ConfigManagementHelper.updateConfigAndClearPool(
                () -> configure.setNamesrvAddr(nameSvrAddrList),
                mqAdminExtPool);
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
        ConfigManagementHelper.updateConfigAndClearPool(
                () -> configure.setIsVIPChannel(useVIPChannel),
                mqAdminExtPool);
        return true;
    }

    @Override
    public boolean updateUseTLS(boolean useTLS) {
        ConfigManagementHelper.updateConfigAndClearPool(
                () -> configure.setUseTLS(useTLS),
                mqAdminExtPool);
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
