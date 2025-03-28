package com.bobby.myrpc.version5.server;

import com.bobby.myrpc.version5.service.IBlogService;
import com.bobby.myrpc.version5.service.IUserService;
import com.bobby.myrpc.version5.service.impl.BlogServiceImpl;
import com.bobby.myrpc.version5.service.impl.UserServiceImpl;

/**
 * version 3: 引入 Netty
 */
public class RPCServerMain {
    public static void main(String[] args) {
        IUserService userService = new UserServiceImpl();
        IBlogService blogService = new BlogServiceImpl();

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.provideServiceInterface(userService);
        serviceProvider.provideServiceInterface(blogService);

        IRPCServer rpcServer = new NettyRPCServer(serviceProvider);
        rpcServer.start(8899);
    }
}