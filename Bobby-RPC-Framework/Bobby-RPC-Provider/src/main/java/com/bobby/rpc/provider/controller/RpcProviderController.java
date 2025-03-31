package com.bobby.rpc.provider.controller;

import com.bobby.rpc.provider.service.IProviderService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rpc/provider/")
public class RpcProviderController {
    @Resource
    IProviderService providerService;

    @PostMapping("/doBiz")
    public void doBiz() {
        providerService.provide();
    }
}
