package com.bobby.rpc.provider.service.impl;

import com.bobby.rpc.core.common.annotation.RpcReference;
import com.bobby.rpc.core.sample.IHelloWorld;
import com.bobby.rpc.provider.service.IProviderService;
import org.springframework.stereotype.Service;

@Service
public class ProviderServiceImpl implements IProviderService {
    @RpcReference
    IHelloWorld helloWorld;

    @Override
    public void provide() {
        System.out.println("Provider invoke:" + helloWorld.helloWorld());
    }
}
