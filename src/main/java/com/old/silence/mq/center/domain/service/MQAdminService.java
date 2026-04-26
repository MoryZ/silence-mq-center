package com.old.silence.mq.center.domain.service;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author moryzang
 */
@Service
public class MQAdminService {

    @Value("${rocketmq.namesrv.addr:110.40.135.216:9876}")
    private String namesrvAddr;

    // 获取 Admin 实例的私有方法
    private DefaultMQAdminExt getAdmin() throws MQClientException {
        DefaultMQAdminExt admin = new DefaultMQAdminExt();
        admin.setNamesrvAddr(namesrvAddr);
        admin.setInstanceName("AdminClient_" + System.currentTimeMillis());
        admin.start();
        return admin;
    }

    // 执行任务的包装方法（自动关停，防止句柄泄露）
    public <T> T execute(AdminTask<T> task) throws Exception {
        DefaultMQAdminExt admin = getAdmin();
        try {
            return task.doTask(admin);
        } finally {
            admin.shutdown();
        }
    }

    // 无返回值任务执行入口
    public void executeVoid(AdminVoidTask task) throws Exception {
        DefaultMQAdminExt admin = getAdmin();
        try {
            task.doTask(admin);
        } finally {
            admin.shutdown();
        }
    }

    @FunctionalInterface
    public interface AdminTask<T> {
        T doTask(DefaultMQAdminExt admin) throws Exception;
    }

    @FunctionalInterface
    public interface AdminVoidTask {
        void doTask(DefaultMQAdminExt admin) throws Exception;
    }
}
