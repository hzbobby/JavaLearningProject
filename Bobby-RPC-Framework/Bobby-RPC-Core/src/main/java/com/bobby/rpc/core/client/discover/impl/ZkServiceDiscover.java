package com.bobby.rpc.core.client.discover.impl;

import com.bobby.rpc.core.client.cache.ServiceCache;
import com.bobby.rpc.core.client.discover.IServiceDiscover;
import com.bobby.rpc.core.client.discover.watcher.ZkWatcher;
import com.bobby.rpc.core.client.loadbalance.ILoadBalance;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZkServiceDiscover implements IServiceDiscover {
    private final CuratorFramework client;
    private final ILoadBalance loadBalance;


    // 既然做了一个本地缓存，缓存添加上去后，服务挂了，谁来更新缓存 ？
    private final ServiceCache serviceCache = new ServiceCache();

    // zk 提供了一种监控机制
    private CuratorCache curatorCache;


    private static final String ROOT_PATH = "BobbyRPC";
    private static final String RETRY = "CanRetry";


    public ZkServiceDiscover(CuratorFramework client, ILoadBalance loadBalance) {
        this.client = client;
        this.loadBalance = loadBalance;
        // 开启服务监听
        ZkWatcher zkWatcher = new ZkWatcher(client, serviceCache);
        zkWatcher.watch(ROOT_PATH);
    }

    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        if (serviceName == null) {
            throw new IllegalArgumentException("Service name cannot be null");
        }
//        String servicePath = getServicePath(serviceName);
        String servicePath = serviceName;
        try {
            // 优先从缓存获取
//            List<String> instances = serviceCache.get(servicePath);
            List<String> instances = serviceCache.getServiceList(serviceName);
            // 没有获取到缓存，则从 zk 中读取
            if (instances == null || instances.isEmpty()) {
                instances = client.getChildren().forPath(servicePath);
                // 缓存 key 是 appName + serviceName
//                serviceCache.put(servicePath, instances);
                serviceCache.addServiceList(serviceName, instances);
            }

            if (instances.isEmpty()) {
                log.warn("未找到可用服务实例: {}", servicePath);
                return null;
            }

            String selectedInstance = loadBalance.balance(instances);
            return parseAddress(selectedInstance);
        } catch (Exception e) {
            log.error("服务发现失败: {}", servicePath, e);
            throw new RuntimeException("Failed to discover service: " + servicePath, e);
        }
    }


    private InetSocketAddress parseAddress(String address) {
        String[] parts = address.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid address format: " + address);
        }
        return new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
    }


}
