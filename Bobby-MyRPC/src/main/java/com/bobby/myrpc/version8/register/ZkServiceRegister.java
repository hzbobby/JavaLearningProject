package com.bobby.myrpc.version8.register;

import com.bobby.myrpc.version8.config.ZkProperties;
import com.bobby.myrpc.version8.loadbalance.ILoadBalance;
import com.bobby.myrpc.version8.loadbalance.RoundLoadBalance;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZkServiceRegister implements IServiceRegister {

    private static final Logger logger = LoggerFactory.getLogger(ZkServiceRegister.class);
    // curator 提供的zookeeper客户端
    private final CuratorFramework client;
    // 自定义负载均衡策略
    private final ILoadBalance loadBalance;
    // 服务缓存
    private final Map<String, List<String>> serviceMap = new ConcurrentHashMap<>();
//    // PathChildrenCache 是 Apache Curator 框架提供的一个高级特性，用于 监听 ZooKeeper 某个路径下子节点的变化
//    private PathChildrenCache cache;

    private CuratorCache curatorCache;

    private final String appName;

    // zookeeper根路径节点
//    我们从配置类中定义
//    private static final String ROOT_PATH = "MyRPC";


    // 通过构造函数注入依赖
    public ZkServiceRegister(String appName, ZkProperties zkProperties) {
        this(appName, zkProperties, new RoundLoadBalance());
    }

    public ZkServiceRegister(String appName, ZkProperties zkProperties, ILoadBalance loadBalance) {
        if (appName == null || appName.isEmpty()) {
            throw new IllegalArgumentException("appName is null or empty");
        }
        if (zkProperties == null) {
            throw new IllegalArgumentException("ZkProperties cannot be null");
        }
        if (loadBalance == null) {
            throw new IllegalArgumentException("LoadBalance cannot be null");
        }
        this.appName = appName;
        this.loadBalance = loadBalance;

        // 使用配置中的参数
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(
                zkProperties.getRetry().getBaseSleepTimeMs(),
                zkProperties.getRetry().getMaxRetries()
        );

        this.client = CuratorFrameworkFactory.builder()
                .connectString(zkProperties.getAddress())   // zk 服务地址 host:port
                .sessionTimeoutMs(zkProperties.getSessionTimeoutMs())
                .retryPolicy(retryPolicy)
                .namespace(zkProperties.getNamespace())
                .build();

        startClient();
    }

    private void startClient() {
        client.start();
        try {
            // 等待连接建立
            client.blockUntilConnected();
            logger.info("Zookeeper连接成功，地址: {}", client.getZookeeperClient().getCurrentConnectionString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Zookeeper连接被中断", e);
            throw new RuntimeException("Failed to connect to Zookeeper", e);
        } catch (Exception e) {
            logger.error("Zookeeper连接失败", e);
            throw new RuntimeException("Failed to connect to Zookeeper", e);
        }
    }

    private String getServicePath(String serviceName) {
        return String.format("/%s/%s", appName, serviceName);
    }

    private String getInstancePath(String serviceName, String addressName) {
        return String.format("/%s/%s/%s", appName, serviceName, addressName);
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
                logger.info("服务实例注册成功: {} -> {}", servicePath, serverAddress);
            } catch (Exception e) {
                // 节点已存在说明该实例正常在线，记录调试日志即可
                logger.debug("服务实例已存在（正常心跳）: {}", addressPath);
            }
        } catch (Exception e) {
            logger.error("服务注册失败: {}", servicePath, e);
            throw new RuntimeException("Failed to register service: " + servicePath, e);
        }
    }

    @Override
    public void remove(String serviceName, InetSocketAddress serverAddress) {
        String instancePath = getInstancePath(serviceName, getServiceAddress(serverAddress));
        try {
            if (client.checkExists().forPath(instancePath) != null) {
                client.delete().forPath(instancePath);
                logger.error("实例下线: {}", instancePath);
            }
        } catch (Exception e) {
            logger.error("移除实例失败: {}", instancePath, e);
            throw new RuntimeException(e);
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
                logger.warn("未找到可用服务实例: {}", servicePath);
                return null;
            }

            String selectedInstance = loadBalance.balance(instances);
            return parseAddress(selectedInstance);
        } catch (Exception e) {
            logger.error("服务发现失败: {}", servicePath, e);
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

    @Override
    public void close() {
        if (curatorCache != null) {
            try {
                curatorCache.close();
            } catch (Exception e) {
                logger.error("关闭PathChildrenCache失败", e);
            }
        }
        if (client != null) {
            client.close();
            logger.info("Zookeeper客户端已关闭");
        }
    }


    @Override
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
                .forInitialized(() -> logger.info("监听器初始化完成: {}", servicePath))
                .build();

        curatorCache.listenable().addListener(listener);
        curatorCache.start();

        logger.info("已创建服务监听: {}", servicePath);
    }

    // 处理节点创建事件
    private void handleNodeCreated(ChildData childData, String servicePath) {
        if (!isDirectChild(childData.getPath(), servicePath)) return;

        updateServiceCache(servicePath);
        logger.debug("服务实例上线: {}", childData.getPath());
    }

    // 处理节点更新事件
    private void handleNodeUpdated(ChildData childData, String servicePath) {
        if (!isDirectChild(childData.getPath(), servicePath)) return;

        updateServiceCache(servicePath);
        logger.debug("服务实例更新: {}", childData.getPath());
    }

    // 处理节点删除事件
    private void handleNodeDeleted(ChildData childData, String servicePath) {
        if (!isDirectChild(childData.getPath(), servicePath)) return;

        updateServiceCache(servicePath);
        logger.debug("服务实例下线: {}", childData.getPath());
    }

    // 更新本地缓存
    private void updateServiceCache(String servicePath) {
        try {
            List<String> instances = client.getChildren().forPath(servicePath);
            serviceMap.put(servicePath, instances);
        } catch (Exception e) {
            logger.error("更新服务缓存失败: {}", servicePath, e);
        }
    }

    // 判断是否为直接子节点（避免孙子节点干扰）
    public boolean isDirectChild(String fullPath, String parentPath) {
        logger.info("fullPath: {}, parentPath: {}, fullPath.substring(parentPath.length()): {}", fullPath, parentPath, fullPath.substring(parentPath.length()));
        return fullPath.startsWith(parentPath) &&
                fullPath.substring(parentPath.length()).lastIndexOf('/') <= 0;
    }

//    // 这里负责zookeeper客户端的初始化，并与zookeeper服务端建立连接
//    public ZkServiceRegister() {
//        // 指数时间重试
//        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
//        // zookeeper的地址固定，不管是服务提供者还是，消费者都要与之建立连接
//        // sessionTimeoutMs 与 zoo.cfg中的tickTime 有关系，
//        // zk还会根据minSessionTimeout与maxSessionTimeout两个参数重新调整最后的超时值。默认分别为tickTime 的2倍和20倍
//        // 使用心跳监听状态
//        this.client = CuratorFrameworkFactory.builder()
//                .connectString("192.168.160.128:2181")
//                .sessionTimeoutMs(40000).retryPolicy(policy).namespace(ROOT_PATH).build();
//        this.client.start();
//        System.out.println("zookeeper 连接成功");
//        loadBalance = new RandomLoadBalance();
//    }

//    @Override
//    public void register(String serviceName, InetSocketAddress serverAddress) {
//        try {
//            // serviceName创建成永久节点，服务提供者下线时，不删服务名，只删地址
//            if (client.checkExists().forPath("/" + serviceName) == null) {
//                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/" + serviceName);
//            }
//            // 路径地址，一个/代表一个节点
//            String path = "/" + serviceName + "/" + getServiceAddress(serverAddress);
//            // 临时节点，服务器下线就删除节点
//            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
//        } catch (Exception e) {
//            System.out.println("此服务已存在");
//        }
//    }

//    // 根据服务名返回地址
//    @Override
//    public InetSocketAddress serviceDiscovery(String serviceName) {
//        // 1. 优先从本地缓存中获取，本地轮询
//        List<String> strings = serviceMap.getOrDefault(serviceName, null);
//        if (strings != null) {
//            String balance = loadBalance.balance(strings);
//            return parseAddress(balance);
//        }
//        try {
//            strings = client.getChildren().forPath("/" + serviceName);
//            // 这里默认用的第一个，后面加负载均衡
//            String instance = loadBalance.balance(strings);
//            return parseAddress(instance);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @Override
//    public void createWatch(String path) {
//        // 在路径前添加斜杠，以确保路径格式正确
//        path = "/" + path;
//
//        // 创建一个 PathChildrenCache 实例，监视指定路径的子节点变化
//        // 第一个参数是 CuratorFramework 客户端实例
//        // 第二个参数是需要监视的路径
//        // 第三个参数指定是否需要递归监视子节点
//        cache = new PathChildrenCache(client, path, true);
//
//        try {
//            // 启动 PathChildrenCache 实例，使其开始监视指定路径
//            cache.start();
//
//            // 添加监听器来处理子节点的变化
//            cache.getListenable().addListener(new PathChildrenCacheListener() {
//                @Override
//                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
//                    // 检查事件类型，处理节点更新或删除事件
//                    if (event.getType() == PathChildrenCacheEvent.Type.CHILD_UPDATED || event.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
//                        // 获取发生变化的节点路径，并将其按照斜杠分割
//                        String[] split = event.getData().getPath().split("/");
//                        // split[1]是发生变化的节点服务名称 向zookeeper重新获取该节点服务下的所有地址
//                        List<String> strings = client.getChildren().forPath("/" + split[1]);
//                        // 更新缓存
//                        serviceMap.put(split[1], strings);
//                    }
//                }
//            });
//        } catch (Exception e) {
//            // 捕获和打印异常信息
//            e.printStackTrace();
//        }
//    }
//
//    // 地址 -> XXX.XXX.XXX.XXX:port 字符串
//    private String getServiceAddress(InetSocketAddress serverAddress) {
//        return serverAddress.getHostName() +
//                ":" +
//                serverAddress.getPort();
//    }
//
//    // 字符串解析为地址
//    private InetSocketAddress parseAddress(String address) {
//        String[] result = address.split(":");
//        return new InetSocketAddress(result[0], Integer.parseInt(result[1]));
//    }
}