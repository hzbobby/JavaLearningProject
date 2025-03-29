package com.bobby.myrpc.version8.server;

import com.bobby.myrpc.version8.service.IBlogService;
import com.bobby.myrpc.version8.service.IUserService;
import com.bobby.myrpc.version8.service.impl.BlogServiceImpl;
import com.bobby.myrpc.version8.service.impl.UserServiceImpl;

/**
 * version 3: 引入 Netty
 */
public class RPCServerMain {
    public static void main(String[] args) {
        IUserService userService = new UserServiceImpl();
        IBlogService blogService = new BlogServiceImpl();

        ServiceProvider serviceProvider = new ServiceProvider("127.0.0.1", 8899);
        serviceProvider.provideServiceInterface(userService);
        serviceProvider.provideServiceInterface(blogService);

        IRPCServer rpcServer = new NettyRPCServer(serviceProvider);
        rpcServer.start(8899);
    }
}