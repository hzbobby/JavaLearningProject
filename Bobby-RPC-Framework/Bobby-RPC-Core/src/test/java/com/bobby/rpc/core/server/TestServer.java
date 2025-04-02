package com.bobby.rpc.core.server;

import com.bobby.rpc.core.common.codec.ISerializer;
import com.bobby.rpc.core.common.spi.SerializerSpiLoader;
import com.bobby.rpc.core.config.properties.BRpcProperties;
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

        BRpcProperties bRpcProperties = BRpcProperties.defaultProperties();

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(
                bRpcProperties.getZk().getRetry().getBaseSleepTimeMs(),
                bRpcProperties.getZk().getRetry().getMaxRetries()
        );

        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("192.168.160.128:2181")   // zk 服务地址 host:port
                .sessionTimeoutMs(bRpcProperties.getZk().getSessionTimeoutMs())
                .retryPolicy(retryPolicy)
                .namespace(bRpcProperties.getZk().getNamespace())
                .build();

        SerializerSpiLoader.loadSpi(ISerializer.class);

        IServiceRegister serviceRegister = new ZkServiceRegister(client);

        ServiceProvider serviceProvider=new ServiceProvider(serviceRegister, new InetSocketAddress("127.0.0.1",9999));

        IDemoService demoService = new DemoServiceImpl();

        serviceProvider.provideServiceInterface(demoService, true);

        IRpcServer rpcServer=new NettyRpcServer(serviceProvider);
        rpcServer.start(9999);
    }
}
