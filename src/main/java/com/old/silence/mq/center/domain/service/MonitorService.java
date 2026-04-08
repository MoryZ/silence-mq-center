package com.old.silence.mq.center.domain.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.mq.center.domain.model.ConsumerMonitor;
import com.old.silence.mq.center.domain.repository.ConsumerMonitorRepository;
import com.old.silence.mq.center.dto.ConsumerMonitorConfig;

/**
 * @author moryzang
 */
@Service
public class MonitorService {

    private final ConsumerMonitorRepository consumerMonitorRepository;

    public MonitorService(ConsumerMonitorRepository consumerMonitorRepository) {
        this.consumerMonitorRepository = consumerMonitorRepository;
    }


    public boolean createOrUpdateConsumerMonitor(String groupName, int minCount, int maxDiffTotal) throws Exception {
        ConsumerMonitor monitor = consumerMonitorRepository.findByQuery(new LambdaQueryWrapper<ConsumerMonitor>()
                .eq(ConsumerMonitor::getGroupName, groupName))
            .stream()
            .findFirst()
            .orElseGet(ConsumerMonitor::new);
        monitor.setGroupName(groupName);
        monitor.setMinCount(minCount);
        monitor.setMaxDiffTotal(maxDiffTotal);
        if (monitor.getId() == null) {
            consumerMonitorRepository.insert(monitor);
        } else {
            consumerMonitorRepository.updateById(monitor);
        }
        return true;
    }

    public Map<String, ConsumerMonitorConfig> queryConsumerMonitorConfig() throws Exception {
        List<ConsumerMonitor> monitorList = consumerMonitorRepository.findByQuery(new LambdaQueryWrapper<>());
        Map<String, ConsumerMonitorConfig> resultMap = new LinkedHashMap<>();
        for (ConsumerMonitor monitor : monitorList) {
            resultMap.put(monitor.getGroupName(), toConfig(monitor));
        }
        return resultMap;
    }

    public ConsumerMonitorConfig queryConsumerMonitorConfigByGroupName(String consumeGroupName) throws Exception {
        ConsumerMonitor monitor = consumerMonitorRepository.findByQuery(new LambdaQueryWrapper<ConsumerMonitor>()
                .eq(ConsumerMonitor::getGroupName, consumeGroupName))
            .stream()
            .findFirst()
            .orElse(null);
        if (monitor == null) {
            return new ConsumerMonitorConfig(0, 0);
        }
        return toConfig(monitor);
    }

    public Boolean deleteConsumerMonitor(String consumeGroupName) {
        consumerMonitorRepository.delete(new LambdaQueryWrapper<ConsumerMonitor>()
            .eq(ConsumerMonitor::getGroupName, consumeGroupName));
        return true;
    }

    private ConsumerMonitorConfig toConfig(ConsumerMonitor monitor) {
        return new ConsumerMonitorConfig(
            monitor.getMinCount() == null ? 0 : monitor.getMinCount(),
            monitor.getMaxDiffTotal() == null ? 0 : monitor.getMaxDiffTotal()
        );
    }
}
