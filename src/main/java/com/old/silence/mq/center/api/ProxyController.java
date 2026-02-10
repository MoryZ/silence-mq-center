package com.old.silence.mq.center.api;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    public Boolean addProxyAddr(@RequestParam String newProxyAddr) {
        proxyService.addProxyAddrList(newProxyAddr);
        return true;
    }

    @PostMapping(value = "/updateProxyAddr")
    public Boolean updateProxyAddr(@RequestParam String proxyAddr) {
        proxyService.updateProxyAddrList(proxyAddr);
        return true;
    }
}
