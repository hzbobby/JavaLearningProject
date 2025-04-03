package com.bobby.rpc.v4.server;

import com.bobby.rpc.v4.sample.service.IBlogService;
import com.bobby.rpc.v4.sample.service.IUserService;
import com.bobby.rpc.v4.sample.service.impl.BlogServiceImpl;
import com.bobby.rpc.v4.sample.service.impl.UserServiceImpl;
import com.bobby.rpc.v4.server.provider.ServiceProvider;
import com.bobby.rpc.v4.server.rpcServer.IRpcServer;
import com.bobby.rpc.v4.server.rpcServer.impl.NettyRpcServer;

public class RpcServerTest {
    public static void main(String[] args) {
        IUserService userService = new UserServiceImpl();
        IBlogService blogService = new BlogServiceImpl();

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.provideServiceInterface(userService);
        serviceProvider.provideServiceInterface(blogService);

//        IRpcServer rpcServer = new ThreadPoolRPCServer(serviceProvider);
        IRpcServer rpcServer = new NettyRpcServer(serviceProvider);
        rpcServer.start(8899);
    }

}
