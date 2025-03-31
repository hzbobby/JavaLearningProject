package com.bobby.rpc.provider.service.impl;

import com.bobby.rpc.core.common.annotation.RpcService;
import com.bobby.rpc.core.sample.IDemoService;
import org.springframework.stereotype.Service;

@Service
@RpcService
public class DemoServiceImpl implements IDemoService {
    @Override
    public String sayHello(String name) {
        return "Provider: Hello, " + name;
    }
}
