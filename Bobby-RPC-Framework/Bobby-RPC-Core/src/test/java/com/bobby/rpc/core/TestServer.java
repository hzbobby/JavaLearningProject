package com.bobby.rpc.core;

import com.bobby.rpc.core.common.constants.ZkConstants;
import com.bobby.rpc.core.sample.IDemoService;
import com.bobby.rpc.core.sample.impl.DemoServiceImpl;
import com.bobby.rpc.core.server.provider.ServiceProvider;
import com.bobby.rpc.core.server.register.IServiceRegister;
import com.bobby.rpc.core.server.register.impl.ZkServiceRegister;
import com.bobby.rpc.core.server.rpcServer.IRpcServer;
import com.bobby.rpc.core.server.rpcServer.impl.NettyRpcServer;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetSocketAddress;

public class TestServer {
    public static void main(String[] args) {

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(
                3000,
                3
        );

        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("192.168.160.128:2181")   // zk 服务地址 host:port
                .sessionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .namespace(ZkConstants.ZK_NAMESPACE)
                .build();


        IServiceRegister serviceRegister = new ZkServiceRegister(client);

        ServiceProvider serviceProvider=new ServiceProvider(serviceRegister, new InetSocketAddress("127.0.0.1",9999));

        IDemoService demoService = new DemoServiceImpl();

        serviceProvider.provideServiceInterface(demoService, true);

        IRpcServer rpcServer=new NettyRpcServer(serviceProvider);
        rpcServer.start(9999);
    }
}
