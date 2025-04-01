package com.bobby.rpc.core.sample.impl;

import com.bobby.rpc.core.sample.IDemoService;

public class DemoServiceImpl implements IDemoService {
    @Override
    public String sayHello(String name) {
        return "DemoServiceImpl sayHello To: " + name;
    }
}
