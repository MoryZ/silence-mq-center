
package com.old.silence.mq.center.factory;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQAdminPooledObjectFactory implements PooledObjectFactory<MQAdminExt> {

    private static final Logger logger = LoggerFactory.getLogger(MQAdminPooledObjectFactory.class);
    private final MQAdminFactory mqAdminFactory;

    public MQAdminPooledObjectFactory(MQAdminFactory mqAdminFactory) {
        this.mqAdminFactory = mqAdminFactory;
    }

    @Override
    public PooledObject<MQAdminExt> makeObject() throws Exception {
        return new DefaultPooledObject<>(
            mqAdminFactory.getInstance());
    }

    @Override
    public void destroyObject(PooledObject<MQAdminExt> p) {
        MQAdminExt mqAdmin = p.getObject();
        if (mqAdmin != null) {
            try {
                mqAdmin.shutdown();
            } catch (Exception e) {
                logger.warn("MQAdminExt shutdown err", e);
            }
        }
        logger.info("destroy object {}", p.getObject());
    }

    @Override
    public boolean validateObject(PooledObject<MQAdminExt> p) {
        MQAdminExt mqAdmin = p.getObject();
        ClusterInfo clusterInfo = null;
        try {
            clusterInfo = mqAdmin.examineBrokerClusterInfo();
        } catch (Exception e) {
            logger.warn("validate object {} err", p.getObject(), e);
        }
        if (clusterInfo == null || MapUtils.isEmpty(clusterInfo.getBrokerAddrTable())) {
            log.warn("validateObject failed, clusterInfo = {}", clusterInfo);
            return false;
        }
        return true;
    }

    @Override
    public void activateObject(PooledObject<MQAdminExt> p) {
    }

    @Override
    public void passivateObject(PooledObject<MQAdminExt> p) {
    }

}
