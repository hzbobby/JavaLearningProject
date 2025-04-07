package com.bobby.rpc.v7.server;

import com.bobby.rpc.v7.sample.service.IBlogService;
import com.bobby.rpc.v7.sample.service.IUserService;
import com.bobby.rpc.v7.sample.service.impl.BlogServiceImpl;
import com.bobby.rpc.v7.sample.service.impl.UserServiceImpl;
import com.bobby.rpc.v7.server.provider.ServiceProvider;
import com.bobby.rpc.v7.server.ratelimit.provider.RateLimitProvider;
import com.bobby.rpc.v7.server.register.IServiceRegister;
import com.bobby.rpc.v7.server.register.impl.ZkServiceRegister;
import com.bobby.rpc.v7.server.rpcServer.IRpcServer;
import com.bobby.rpc.v7.server.rpcServer.impl.NettyRpcServer;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetSocketAddress;


public class RpcServerTest {

    public static void main(String[] args) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(
                1000,
                3
        );

        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("192.168.232.129:2181")   // zk 服务地址 host:port
//                .connectString("192.168.160.128:2181")   // zk 服务地址 host:port
                .sessionTimeoutMs(30000)
                .retryPolicy(retryPolicy)
                .namespace("BobbyRPC")
                .build();

        IServiceRegister serviceRegister = new ZkServiceRegister(client);

        IUserService userService = new UserServiceImpl();
        IBlogService blogService = new BlogServiceImpl();

        RateLimitProvider rateLimitProvider = new RateLimitProvider();

        ServiceProvider serviceProvider = new ServiceProvider(serviceRegister, new InetSocketAddress("127.0.0.1", 8899), rateLimitProvider);
        serviceProvider.provideServiceInterface(userService);
        serviceProvider.provideServiceInterface(blogService);

//        IRpcServer rpcServer = new ThreadPoolRPCServer(serviceProvider);
        IRpcServer rpcServer = new NettyRpcServer(serviceProvider);
        rpcServer.start(8899);
    }

}
