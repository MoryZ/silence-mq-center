
package com.old.silence.mq.center.domain.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractCommonService {
    protected final MQAdminExt mqAdminExt;

    protected AbstractCommonService(MQAdminExt mqAdminExt) {
        this.mqAdminExt = mqAdminExt;
    }

    protected final Set<String> changeToBrokerNameSet(Map<String, Set<String>> clusterAddrTable,
        List<String> clusterNameList, List<String> brokerNameList) {
        Set<String> finalBrokerNameList = Sets.newHashSet();
        if (CollectionUtils.isNotEmpty(clusterNameList)) {
            try {
                for (String clusterName : clusterNameList) {
                    finalBrokerNameList.addAll(clusterAddrTable.get(clusterName));
                }
            }
            catch (Exception e) {
                Throwables.throwIfUnchecked(e);
                throw new RuntimeException(e);
            }
        }
        if (CollectionUtils.isNotEmpty(brokerNameList)) {
            finalBrokerNameList.addAll(brokerNameList);
        }
        return finalBrokerNameList;
    }
}
