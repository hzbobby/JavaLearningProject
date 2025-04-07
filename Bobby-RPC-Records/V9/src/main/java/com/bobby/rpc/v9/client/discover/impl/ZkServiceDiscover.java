package com.bobby.rpc.v9.client.discover.impl;


import com.bobby.rpc.v9.client.cache.ServiceCache;
import com.bobby.rpc.v9.client.discover.IServiceDiscover;
import com.bobby.rpc.v9.client.discover.watcher.ZkWatcher;
import com.bobby.rpc.v9.common.loadbalance.ILoadBalance;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZkServiceDiscover implements IServiceDiscover {
    private final CuratorFramework client;
    private final ILoadBalance loadBalance;


    // 既然做了一个本地缓存，缓存添加上去后，服务挂了，谁来更新缓存 ？
    private final ServiceCache serviceCache ;
    private final ZkWatcher zkWatcher;

    public ZkServiceDiscover(CuratorFramework client, ILoadBalance loadBalance) {
        this.client = client;
        this.loadBalance = loadBalance;

        if(!client.getState().equals(CuratorFrameworkState.STARTED)){
            this.client.start();
        }

        serviceCache = new ServiceCache();
        // v6
        // 开启服务监听
        zkWatcher = new ZkWatcher(client, serviceCache);
//        // 监控根目录
//        zkWatcher.watch("/BobbyRPC");    // 监控的 根路径
        // 服务发现的话，一般只需监控自己所用的服务下的实例节点就好把？
        // 监控整个根路径反而会带来较大的性能开销
    }

    private String getServicePath(String serviceName) {
        return String.format("/%s", serviceName);
    }

    private String getInstancePath(String serviceName, String addressName) {
        return String.format("/%s/%s",  serviceName, addressName);
    }

    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        if (serviceName == null) {
            throw new IllegalArgumentException("Service name cannot be null");
        }
        String servicePath = getServicePath(serviceName);
        try {
            // 优先从缓存获取
            List<String> instances = serviceCache.getServiceList(servicePath);
            // 没有获取到缓存，则从 zk 中读取
            if (instances == null || instances.isEmpty()) {
                instances = client.getChildren().forPath(servicePath);
                // 缓存 key 是 appName + serviceName
//                serviceCache.put(servicePath, instances);
                serviceCache.addServiceList(servicePath, instances);

                // v6
                // 因此我们在服务发现的时候，动态的进行监控
                // 如果缓存中没有，表示是第一次获取，那么我们就对这些服务实例进行监控
                // 当这些服务实例发生变动时，就通知客户端
                // 有了缓存机制，就不用显示的往缓存里添加，不然可能会添加两次
                zkWatcher.watch(servicePath);
            }

            if (instances.isEmpty()) {
                log.warn("未找到可用服务实例: {}", servicePath);
                return null;
            }
            // 未进行负载均衡，选择第一个
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

    @Override
    public boolean retryable(String serviceName) {
        boolean canRetry =false;
        try {
            List<String> serviceList = client.getChildren().forPath("/RETRY");
            for(String s:serviceList){
                if(s.equals(serviceName)){
                    log.debug("服务: {} 在白名单上，可以进行重试", serviceName);
                    canRetry=true;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return canRetry;
    }
}
