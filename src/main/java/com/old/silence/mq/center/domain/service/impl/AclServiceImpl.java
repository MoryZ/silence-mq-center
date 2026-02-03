
package com.old.silence.mq.center.domain.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.AclConfig;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.PlainAccessConfig;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.google.common.base.Throwables;
import com.old.silence.mq.center.domain.model.request.AclRequest;
import com.old.silence.mq.center.domain.service.AbstractCommonService;
import com.old.silence.mq.center.domain.service.AclService;
import com.old.silence.mq.center.domain.service.facade.RocketMQClientFacade;
import com.old.silence.mq.center.domain.service.helper.AclConfigHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AclServiceImpl extends AbstractCommonService implements AclService {


    private static final Logger log = LoggerFactory.getLogger(AclServiceImpl.class);
    
    private final RocketMQClientFacade mqFacade;

    protected AclServiceImpl(MQAdminExt mqAdminExt, RocketMQClientFacade mqFacade) {
        super(mqAdminExt);
        this.mqFacade = mqFacade;
    }

    @Override
    public AclConfig getAclConfig(boolean excludeSecretKey) {
        try {
            Optional<String> addr = getMasterSet().stream().findFirst();
            if (addr.isPresent()) {
                if (!excludeSecretKey) {
                    return mqFacade.getAclConfig(addr.get());
                } else {
                    AclConfig aclConfig = mqFacade.getAclConfig(addr.get());
                    if (CollectionUtils.isNotEmpty(aclConfig.getPlainAccessConfigs())) {
                        aclConfig.getPlainAccessConfigs().forEach(pac -> pac.setSecretKey(null));
                    }
                    return aclConfig;
                }
            }
        } catch (Exception e) {
            log.error("getAclConfig error.", e);
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
        AclConfig aclConfig = new AclConfig();
        aclConfig.setGlobalWhiteAddrs(Collections.emptyList());
        aclConfig.setPlainAccessConfigs(Collections.emptyList());
        return aclConfig;
    }

    @Override
    public void addAclConfig(PlainAccessConfig config) {
        try {
            Set<String> masterSet = getMasterSet();

            if (masterSet.isEmpty()) {
                throw new IllegalStateException("broker addr list is empty");
            }

            // 检查accessKey是否已存在
            for (String addr : masterSet) {
                AclConfig aclConfig = mqFacade.getAclConfig(addr);
                Optional<PlainAccessConfig> existing = AclConfigHelper.findAccessKeyConfig(aclConfig, config.getAccessKey());
                if (existing.isPresent()) {
                    throw new IllegalArgumentException(String.format("broker: %s, exist accessKey: %s", addr, config.getAccessKey()));
                }
            }

            // 在所有Broker上创建或更新配置
            AclConfigHelper.executeBrokerOperation(getBrokerAddrs(), addr -> 
                mqFacade.createOrUpdatePlainAccessConfig(addr, config)
            );
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAclConfig(PlainAccessConfig config) {
        AclConfigHelper.executeBrokerOperation(getBrokerAddrs(), addr -> {
            log.info("Start to delete acl [{}] from broker [{}]", config.getAccessKey(), addr);
            if (isExistAccessKey(config.getAccessKey(), addr)) {
                mqFacade.deletePlainAccessConfig(addr, config.getAccessKey());
            }
            log.info("Delete acl [{}] from broker [{}] complete", config.getAccessKey(), addr);
        });
    }

    @Override
    public void updateAclConfig(PlainAccessConfig config) {
        for (String addr : getBrokerAddrs()) {
            AclConfig aclConfig = mqFacade.getAclConfig(addr);
            Optional<PlainAccessConfig> existing = AclConfigHelper.findAccessKeyConfig(aclConfig, config.getAccessKey());
            PlainAccessConfig targetConfig = config;
            if (existing.isPresent()) {
                PlainAccessConfig remoteConfig = existing.get();
                remoteConfig.setSecretKey(config.getSecretKey());
                remoteConfig.setAdmin(config.isAdmin());
                targetConfig = remoteConfig;
            }
            mqFacade.createOrUpdatePlainAccessConfig(addr, targetConfig);
        }
    }

    @Override
    public void addOrUpdateAclTopicConfig(AclRequest request) {
        for (String addr : getBrokerAddrs()) {
            AclConfig aclConfig = mqFacade.getAclConfig(addr);
            PlainAccessConfig remoteConfig = AclConfigHelper.findAccessKeyConfig(aclConfig, request.getConfig().getAccessKey()).orElse(null);
            if (remoteConfig == null) {
                mqFacade.createOrUpdatePlainAccessConfig(addr, request.getConfig());
            } else {
                if (remoteConfig.getTopicPerms() == null) {
                    remoteConfig.setTopicPerms(new ArrayList<>());
                }
                String topicName = AclConfigHelper.extractPermName(request.getTopicPerm());
                AclConfigHelper.removePermByName(remoteConfig.getTopicPerms(), topicName);
                remoteConfig.getTopicPerms().add(request.getTopicPerm());
                mqFacade.createOrUpdatePlainAccessConfig(addr, remoteConfig);
            }
        }
    }

    @Override
    public void addOrUpdateAclGroupConfig(AclRequest request) {
        for (String addr : getBrokerAddrs()) {
            AclConfig aclConfig = mqFacade.getAclConfig(addr);
            PlainAccessConfig remoteConfig = AclConfigHelper.findAccessKeyConfig(aclConfig, request.getConfig().getAccessKey()).orElse(null);
            if (remoteConfig == null) {
                mqFacade.createOrUpdatePlainAccessConfig(addr, request.getConfig());
            } else {
                if (remoteConfig.getGroupPerms() == null) {
                    remoteConfig.setGroupPerms(new ArrayList<>());
                }
                String groupName = AclConfigHelper.extractPermName(request.getGroupPerm());
                AclConfigHelper.removePermByName(remoteConfig.getGroupPerms(), groupName);
                remoteConfig.getGroupPerms().add(request.getGroupPerm());
                mqFacade.createOrUpdatePlainAccessConfig(addr, remoteConfig);
            }
        }
    }

    @Override
    public void deletePermConfig(AclRequest request) {
        try {
            PlainAccessConfig deleteConfig = request.getConfig();
            String topic = StringUtils.isNotEmpty(request.getTopicPerm()) ? AclConfigHelper.extractPermName(request.getTopicPerm()) : null;
            String group = StringUtils.isNotEmpty(request.getGroupPerm()) ? AclConfigHelper.extractPermName(request.getGroupPerm()) : null;

            if (deleteConfig.getTopicPerms() != null && topic != null) {
                AclConfigHelper.removePermByName(deleteConfig.getTopicPerms(), topic);
            }
            if (deleteConfig.getGroupPerms() != null && group != null) {
                AclConfigHelper.removePermByName(deleteConfig.getGroupPerms(), group);
            }

            for (String addr : getBrokerAddrs()) {
                AclConfig aclConfig = mqFacade.getAclConfig(addr);
                PlainAccessConfig remoteConfig = AclConfigHelper.findAccessKeyConfig(aclConfig, deleteConfig.getAccessKey()).orElse(null);
                if (remoteConfig == null) {
                    mqFacade.createOrUpdatePlainAccessConfig(addr, deleteConfig);
                } else {
                    if (remoteConfig.getTopicPerms() != null && topic != null) {
                        AclConfigHelper.removePermByName(remoteConfig.getTopicPerms(), topic);
                    }
                    if (remoteConfig.getGroupPerms() != null && group != null) {
                        AclConfigHelper.removePermByName(remoteConfig.getGroupPerms(), group);
                    }
                    mqFacade.createOrUpdatePlainAccessConfig(addr, remoteConfig);
                }
            }
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void syncData(PlainAccessConfig config) {
        try {
            for (String addr : getBrokerAddrs()) {
                mqFacade.createOrUpdatePlainAccessConfig(addr, config);
            }
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addWhiteList(List<String> whiteList) {
        if (whiteList == null) {
            return;
        }
        for (String addr : getBrokerAddrs()) {
            AclConfig aclConfig = mqFacade.getAclConfig(addr);
            if (aclConfig.getGlobalWhiteAddrs() != null) {
                aclConfig.setGlobalWhiteAddrs(Stream.of(whiteList, aclConfig.getGlobalWhiteAddrs())
                        .flatMap(Collection::stream).distinct().collect(Collectors.toList()));
            } else {
                aclConfig.setGlobalWhiteAddrs(whiteList);
            }
            mqFacade.updateGlobalWhiteAddrConfig(addr, StringUtils.join(aclConfig.getGlobalWhiteAddrs(), ","));
        }
    }

    @Override
    public void deleteWhiteAddr(String deleteAddr) {
        for (String addr : getBrokerAddrs()) {
            AclConfig aclConfig = mqFacade.getAclConfig(addr);
            if (aclConfig.getGlobalWhiteAddrs() == null || aclConfig.getGlobalWhiteAddrs().isEmpty()) {
                continue;
            }
            aclConfig.getGlobalWhiteAddrs().remove(deleteAddr);
            mqFacade.updateGlobalWhiteAddrConfig(addr, StringUtils.join(aclConfig.getGlobalWhiteAddrs(), ","));
        }
    }

    @Override
    public void synchronizeWhiteList(List<String> whiteList) {
        if (whiteList == null) {
            return;
        }
        AclConfigHelper.executeBrokerOperation(getBrokerAddrs(), addr -> 
            mqFacade.updateGlobalWhiteAddrConfig(addr, StringUtils.join(whiteList, ","))
        );
    }

    private void removeExist(List<String> list, String name) {
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String v = iterator.next();
            String cmp = v.split("=")[0];
            if (cmp.equals(name)) {
                iterator.remove();
            }
        }
    }

    private boolean isExistAccessKey(String accessKey, String addr) throws Exception {
        AclConfig aclConfig = mqFacade.getAclConfig(addr);
        List<PlainAccessConfig> configs = aclConfig.getPlainAccessConfigs();
        if (configs == null || configs.isEmpty()) {
            return false;
        }
        return configs.stream().anyMatch(c -> accessKey.equals(c.getAccessKey()));
    }

    private Set<BrokerData> getBrokerDataSet() throws InterruptedException, RemotingConnectException, RemotingTimeoutException, RemotingSendRequestException, MQBrokerException {
        ClusterInfo clusterInfo = mqFacade.getRawClusterInfo();
        Map<String, BrokerData> brokerDataMap = clusterInfo.getBrokerAddrTable();
        return new HashSet<>(brokerDataMap.values());
    }

    private Set<String> getMasterSet() throws InterruptedException, MQBrokerException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException {
        return getBrokerDataSet().stream().map(data -> data.getBrokerAddrs().get(MixAll.MASTER_ID)).collect(Collectors.toSet());
    }

    private Set<String> getBrokerAddrs() throws InterruptedException, MQBrokerException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException {
        Set<String> brokerAddrs = new HashSet<>();
        getBrokerDataSet().forEach(data -> brokerAddrs.addAll(data.getBrokerAddrs().values()));
        return brokerAddrs;
    }
}
