package com.bobby.myrpc.version2.server;

import com.bobby.myrpc.version2.service.IBlogService;
import com.bobby.myrpc.version2.service.IUserService;
import com.bobby.myrpc.version2.service.impl.BlogServiceImpl;
import com.bobby.myrpc.version2.service.impl.UserServiceImpl;

/**
 * version 2: 降低耦合度,引入服务提供者
 */
public class RPCServerMain {
    public static void main(String[] args) {
        IUserService userService = new UserServiceImpl();
        IBlogService blogService = new BlogServiceImpl();

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.provideServiceInterface(userService);
        serviceProvider.provideServiceInterface(blogService);

        IRPCServer rpcServer = new SimpleRPCServer(serviceProvider);
        rpcServer.start(8899);
    }
}