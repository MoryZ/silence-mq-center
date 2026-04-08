package com.old.silence.mq.center.api;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.old.silence.mq.center.domain.service.ProxyService;

import java.util.Map;


@RestController
@RequestMapping("/api/v1/proxy")
public class ProxyController {
    private final ProxyService proxyService;

    public ProxyController(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @GetMapping(value = "/homePage")
    public Map<String, Object> homePage() {
        return proxyService.getProxyHomePage();
    }

    @PostMapping(value = "/addProxyAddr")
    public int addProxyAddr(@RequestParam String newProxyAddr) {
        return proxyService.addProxyNode(newProxyAddr);
    }

    @PutMapping(value = "/updateProxyAddr")
    public int updateProxyAddr(@RequestParam String proxyAddr) {
        return proxyService.updateProxyAddrList(proxyAddr);
    }
}
