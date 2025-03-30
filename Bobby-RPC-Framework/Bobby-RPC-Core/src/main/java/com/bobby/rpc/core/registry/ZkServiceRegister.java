package com.bobby.rpc.core.registry;

import com.bobby.myrpc.version8.common.loadbalance.ILoadBalance;
import com.bobby.myrpc.version8.config.BRpcProperties;
import com.bobby.myrpc.version8.register.IServiceRegister;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.zookeeper.CreateMode;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ZkServiceRegister implements IServiceRegister {

    private final BRpcProperties rpcProperties;
    private final ILoadBalance loadBalance;
    private final CuratorFramework client;
    private final Map<String, List<String>> serviceMap = new ConcurrentHashMap<>();
    private CuratorCache curatorCache;

    public ZkServiceRegister(BRpcProperties rpcProperties, ILoadBalance loadBalance, CuratorFramework client) {
        this.rpcProperties = rpcProperties;
        this.loadBalance = loadBalance;
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
        return String.format("/%s/%s", rpcProperties.getApplicationName(), serviceName);
    }

    private String getInstancePath(String serviceName, String addressName) {
        return String.format("/%s/%s/%s", rpcProperties.getApplicationName(), serviceName, addressName);
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
//            String addressPath = servicePath + "/" + getServiceAddress(serverAddress);
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
        } catch (Exception e) {
            log.error("服务注册失败: {}", servicePath, e);
            throw new RuntimeException("Failed to register service: " + servicePath, e);
        }

        if (rpcProperties.getWatch() != null && rpcProperties.getWatch()) {
            log.info("服务开启监控: application: {}, serviceName: {}", rpcProperties.getApplicationName(), servicePath);
            watch(serviceName);
        }
    }

    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        if (serviceName == null) {
            throw new IllegalArgumentException("Service name cannot be null");
        }
        String servicePath = getServicePath(serviceName);
        try {
            // 优先从缓存获取
            List<String> instances = serviceMap.get(servicePath);
            // 没有获取到缓存，则从 zk 中读取
            if (instances == null || instances.isEmpty()) {
                instances = client.getChildren().forPath(servicePath);
                // 缓存 key 是 appName + serviceName
                serviceMap.put(servicePath, instances);
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

    private String getServiceAddress(InetSocketAddress serverAddress) {
        return serverAddress.getHostName() + ":" + serverAddress.getPort();
    }

    private InetSocketAddress parseAddress(String address) {
        String[] parts = address.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid address format: " + address);
        }
        return new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
    }

    public void watch(String serviceName) {
        if (serviceName == null) {
            throw new IllegalArgumentException("Service name cannot be null");
        }
        String servicePath = getServicePath(serviceName);
        String watchPath = servicePath;

        // 关闭旧的监听器（如果存在）
        if (this.curatorCache != null) {
            this.curatorCache.close();
        }

        // 创建新的 CuratorCache
        this.curatorCache = CuratorCache.build(client, watchPath);

        // 添加监听器
        // 分别在创建时，改变时，删除时对本地缓存进行改动
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forCreates(childData -> handleNodeCreated(childData, servicePath))
                .forChanges((oldData, newData) -> handleNodeUpdated(newData, servicePath))
                .forDeletes(childData -> handleNodeDeleted(childData, servicePath))
                .forInitialized(() -> log.info("监听器初始化完成: {}", servicePath))
                .build();

        curatorCache.listenable().addListener(listener);
        curatorCache.start();

        log.info("已创建服务监听: {}", servicePath);
    }

    // 处理节点创建事件
    private void handleNodeCreated(ChildData childData, String servicePath) {
        if (!isDirectChild(childData.getPath(), servicePath)) return;

        updateServiceCache(servicePath);
        log.debug("服务实例上线: {}", childData.getPath());
    }

    // 处理节点更新事件
    private void handleNodeUpdated(ChildData childData, String servicePath) {
        if (!isDirectChild(childData.getPath(), servicePath)) return;

        updateServiceCache(servicePath);
        log.debug("服务实例更新: {}", childData.getPath());
    }

    // 处理节点删除事件
    private void handleNodeDeleted(ChildData childData, String servicePath) {
        if (!isDirectChild(childData.getPath(), servicePath)) return;

        updateServiceCache(servicePath);
        log.debug("服务实例下线: {}", childData.getPath());
    }

    // 更新本地缓存
    private void updateServiceCache(String servicePath) {
        try {
            List<String> instances = client.getChildren().forPath(servicePath);
            serviceMap.put(servicePath, instances);
        } catch (Exception e) {
            log.error("更新服务缓存失败: {}", servicePath, e);
        }
    }

    // 判断是否为直接子节点（避免孙子节点干扰）
    public boolean isDirectChild(String fullPath, String parentPath) {
        log.info("fullPath: {}, parentPath: {}, fullPath.substring(parentPath.length()): {}", fullPath, parentPath, fullPath.substring(parentPath.length()));
        return fullPath.startsWith(parentPath) &&
                fullPath.substring(parentPath.length()).lastIndexOf('/') <= 0;
    }
}