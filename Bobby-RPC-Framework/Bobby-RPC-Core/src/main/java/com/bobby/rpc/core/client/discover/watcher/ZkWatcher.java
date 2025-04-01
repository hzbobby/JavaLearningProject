package com.bobby.rpc.core.client.discover.watcher;

import com.bobby.rpc.core.client.cache.ServiceCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

import java.util.List;

@Slf4j
public class ZkWatcher {
    private final CuratorFramework client;
    private final ServiceCache cache;

    public ZkWatcher(CuratorFramework client, ServiceCache cache) {
        this.client = client;
        this.cache = cache;
    }

    public void watch(String watchPath) {
        if (watchPath == null) {
            throw new IllegalArgumentException("Service name cannot be null");
        }
//        String servicePath = getServicePath(serviceName);


        // 创建新的 CuratorCache
        CuratorCache curatorCache = CuratorCache.build(client, watchPath);

        // 添加监听器
        // 分别在创建时，改变时，删除时对本地缓存进行改动
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forCreates(childData -> handleNodeCreated(childData, parseServiceName(childData)))
//                .forChanges((oldData, newData) -> handleNodeUpdated(newData, parseServiceName(childData)))
//                .forDeletes(childData -> handleNodeDeleted(childData, parseServiceName(childData)))
//                .forInitialized(() -> log.info("监听器初始化完成: {}", parseServiceName(childData)))
                .build();

//        curatorCache.listenable().addListener(listener);
//        curatorCache.start();

        log.debug("已创建服务监听");
    }

    private String parseServiceName(ChildData childData){
        String s = new String(childData.getData());
        return s;
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
            cache.addServiceList(servicePath, instances);
        } catch (Exception e) {
            log.error("更新服务缓存失败: {}", servicePath, e);
        }
    }

    // 判断是否为直接子节点（避免孙子节点干扰）
    public boolean isDirectChild(String fullPath, String parentPath) {
        return fullPath.startsWith(parentPath) &&
                fullPath.substring(parentPath.length()).lastIndexOf('/') <= 0;
    }
}
