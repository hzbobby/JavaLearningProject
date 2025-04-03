package com.bobby.rpc.v5.server.register.impl;


import com.bobby.rpc.v5.server.register.IServiceRegister;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;

@Slf4j
public class ZkServiceRegister implements IServiceRegister {

    private final CuratorFramework client;

    public ZkServiceRegister(CuratorFramework client) {
        this.client = client;
        startClient();
    }

    private void startClient() {
        client.start();
        try {
            // 等待连接建立
            client.blockUntilConnected();
            log.info("Zookeeper连接成功，地址: {}", client.getZookeeperClient().getCurrentConnectionString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Zookeeper连接被中断", e);
            throw new RuntimeException("Failed to connect to Zookeeper", e);
        } catch (Exception e) {
            log.error("Zookeeper连接失败", e);
            throw new RuntimeException("Failed to connect to Zookeeper", e);
        }
    }

    private String getServicePath(String serviceName) {
        return String.format("/%s", serviceName);
    }

    private String getInstancePath(String serviceName, String addressName) {
        return String.format("/%s/%s",  serviceName, addressName);
    }


    @Override
    public void register(String serviceName, InetSocketAddress serverAddress) {
        if (serviceName == null || serverAddress == null) {
            throw new IllegalArgumentException("Service name and server address cannot be null");
        }
        String servicePath = getServicePath(serviceName);

        try {
            // 1. 创建持久化父节点（幂等操作） -- 一般是服务的分类，例如一个服务名
            if (client.checkExists().forPath(servicePath) == null) {
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(servicePath);
            }

            // 2. 注册临时节点（允许重复创建，实际会覆盖）-- 一般是具体的实例，服务名下，不同的实例
            String addressPath = getInstancePath(serviceName, getServiceAddress(serverAddress));
            try {
                client.create()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(addressPath);
                log.info("服务实例注册成功: {} -> {}", servicePath, serverAddress);
            } catch (Exception e) {
                // 节点已存在说明该实例正常在线，记录调试日志即可
                log.debug("服务实例已存在（正常心跳）: {}", addressPath);
            }

//            // 3. 创建 Retry 节点
//            if(retryable){
//                client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(String.format("/%s/%s", ZkConstants.RETRY, serviceName));
//            }

        } catch (Exception e) {
            log.error("服务注册失败: {}", servicePath, e);
            throw new RuntimeException("Failed to register service: " + servicePath, e);
        }
    }


    private String getServiceAddress(InetSocketAddress serverAddress) {
        return serverAddress.getHostName() + ":" + serverAddress.getPort();
    }
}