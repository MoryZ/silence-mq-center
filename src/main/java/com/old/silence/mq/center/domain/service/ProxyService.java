package com.old.silence.mq.center.domain.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.mq.center.domain.model.ProxyNode;
import com.old.silence.mq.center.domain.repository.ProxyNodeRepository;
import com.old.silence.mq.center.util.NetUtils;
import com.old.silence.mq.center.vo.ProxyStatus;

/**
 * @author moryzang
 */
@Service
public class ProxyService {

    private final ProxyNodeRepository proxyNodeRepository;

    public ProxyService(ProxyNodeRepository proxyNodeRepository) {
        this.proxyNodeRepository = proxyNodeRepository;
    }

    public Map<String, Object> getProxyHomePage() {
        Map<String, Object> stats = new HashMap<>();
        List<String> proxyList = CollectionUtils.transformToList(proxyNodeRepository.findAll()
                , ProxyNode::getProxyAddr);

        List<ProxyStatus> statusList = proxyList.parallelStream().map(addr -> {
            // 1. 简单的端口检查
            boolean alive = NetUtils.isAddressReachable(addr);

            // 2. 进阶：请求 Proxy 的健康检查接口 (如果配置了)
            // 5.x Proxy 通常在 8080/8081 暴露状态
            return new ProxyStatus(addr, alive);
        }).collect(Collectors.toList());

        stats.put("proxyList", statusList);
        stats.put("totalCount", proxyList.size());
        stats.put("activeCount", statusList.stream().filter(ProxyStatus::isAlive).count());
        return stats;
    }

    public int addProxyNode(String newProxyAddr) {
        // 1. 简单的正则校验 IP:Port 格式
        if (!newProxyAddr.matches("^[^:]+:\\d+$")) {
            throw new RuntimeException("Proxy 地址格式错误，需为 IP:Port");
        }

        // 2. 检查是否已存在 (避免重复)
        QueryWrapper<ProxyNode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("proxy_addr", newProxyAddr);
        if (proxyNodeRepository.selectCount(queryWrapper) > 0) {
            throw new SecurityException() ; // 或者抛出异常提醒已存在
        }

        // 3. 插入数据库 (MyBatis-Plus)
        ProxyNode node = new ProxyNode();
        node.setProxyAddr(newProxyAddr);
        return proxyNodeRepository.insert(node);

    }

    public int updateProxyAddrList(String proxyAddr) {
        // 这里的逻辑取决于你前端传的是什么
        // 如果是覆盖式更新：
        UpdateWrapper<ProxyNode> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("proxy_addr", proxyAddr);
        return proxyNodeRepository.update(null, updateWrapper);
    }
}
