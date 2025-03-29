package com.bobby.bizb.service.impl;

import com.bobby.common.service.IDubboDemoService;
import com.bobby.common.utils.Result;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

// 使用这个注解，可以将该接口实现注册到注册中心
@DubboService
@Service
public class DubboDemoServiceImpl implements IDubboDemoService {
    @Override
    public Result getDemo() {
        return Result.ok("Hi, it's dubbo remote service. ");
    }
}
