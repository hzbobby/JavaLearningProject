package com.bobby.rpc.consumer.service.impl;

import com.bobby.rpc.consumer.service.IConsumerService;
import com.bobby.rpc.core.common.annotation.RpcReference;
import com.bobby.rpc.core.sample.IDemoService;
import org.springframework.stereotype.Service;

@Service
public class ConsumerServiceImpl implements IConsumerService {
    @RpcReference
    IDemoService demoService;

    @Override
    public void consume() {
        System.out.println("Consumer Invoke:" + demoService.sayHello("Bobby"));
    }
}
