
package com.old.silence.mq.center.factory;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.old.silence.mq.center.api.config.RMQConfigure;

import java.util.concurrent.atomic.AtomicLong;

public class MQAdminFactory {


    private static final Logger log = LoggerFactory.getLogger(MQAdminFactory.class);
    private final RMQConfigure rmqConfigure;

    public MQAdminFactory(RMQConfigure rmqConfigure) {
        this.rmqConfigure = rmqConfigure;
    }

    private final AtomicLong adminIndex = new AtomicLong(0);

    public MQAdminExt getInstance() throws Exception {
        RPCHook rpcHook = null;
        String accessKey = rmqConfigure.getAccessKey();
        String secretKey = rmqConfigure.getSecretKey();
        boolean isEnableAcl = StringUtils.isNotEmpty(accessKey) && StringUtils.isNotEmpty(secretKey);
        if (isEnableAcl) {
            rpcHook = new AclClientRPCHook(new SessionCredentials(accessKey, secretKey));
        }
        DefaultMQAdminExt mqAdminExt;
        if (rmqConfigure.getTimeoutMillis() == null) {
            mqAdminExt = new DefaultMQAdminExt(rpcHook);
        } else {
            mqAdminExt = new DefaultMQAdminExt(rpcHook, rmqConfigure.getTimeoutMillis());
        }
        mqAdminExt.setAdminExtGroup(mqAdminExt.getAdminExtGroup() + "_" + adminIndex.getAndIncrement());
        mqAdminExt.setVipChannelEnabled(Boolean.parseBoolean(rmqConfigure.getIsVIPChannel()));
        mqAdminExt.setUseTLS(rmqConfigure.isUseTLS());
        mqAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
        mqAdminExt.start();
        log.info("create MQAdmin instance {} success.", mqAdminExt);
        return mqAdminExt;
    }
}
