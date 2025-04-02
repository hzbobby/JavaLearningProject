package com.bobby.rpc.core.sample.impl;

import com.bobby.rpc.core.sample.IDemoService;

public class DemoServiceImpl implements IDemoService {
    @Override
    public String sayHello(String name) {
//        try {
//            Thread.sleep(30000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        return "DemoServiceImpl sayHello To: " + name;
    }
}
