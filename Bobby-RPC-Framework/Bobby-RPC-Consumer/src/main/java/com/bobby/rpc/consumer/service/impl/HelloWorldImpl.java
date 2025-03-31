package com.bobby.rpc.consumer.service.impl;

import com.bobby.rpc.core.common.annotation.RpcService;
import com.bobby.rpc.core.sample.IHelloWorld;
import org.springframework.stereotype.Service;


@RpcService
@Service
public class HelloWorldImpl implements IHelloWorld {
    @Override
    public String helloWorld() {
        return "Hello World from Rpc Consumer";
    }
}
