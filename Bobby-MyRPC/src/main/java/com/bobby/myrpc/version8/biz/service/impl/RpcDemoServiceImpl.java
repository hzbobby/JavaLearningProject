package com.bobby.myrpc.version8.biz.service.impl;

import com.bobby.myrpc.version8.biz.domain.User;
import com.bobby.myrpc.version8.biz.service.IRpcDemoService;
import com.bobby.myrpc.version8.biz.service.IUserService;
import com.bobby.myrpc.version8.common.annotation.RpcReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/30
 */
@Slf4j
@Service
public class RpcDemoServiceImpl implements IRpcDemoService {
    @RpcReference
    IUserService userService;

    @Override
    public void doSomething() {
        User userById = userService.getUserById(98L);
        log.info("doSomething: " + userById);
    }
}
