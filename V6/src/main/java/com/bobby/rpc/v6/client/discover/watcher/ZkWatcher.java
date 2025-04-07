package com.bobby.rpc.v6.client.discover.watcher;

import com.bobby.rpc.v6.client.cache.ServiceCache;
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
    private String currentWatchPath;

    public ZkWatcher(CuratorFramework client, ServiceCache cache) {
        this.client = client;
        this.cache = cache;
    }

    public void watch(String watchPath) {
        if (watchPath == null) {
            throw new IllegalArgumentException("Service name cannot be null");
        }
//        String servicePath = getServicePath(serviceName);
        this.currentWatchPath = watchPath;

        // 创建新的 CuratorCache
        CuratorCache curatorCache = CuratorCache.build(client, watchPath);
        curatorCache.start();


        // 添加监听器
        // 分别在创建时，改变时，删除时对本地缓存进行改动
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forCreates(this::handleNodeCreated)
                .forChanges(this::handleNodeUpdated)
                .forDeletes(this::handleNodeDeleted)
                .forInitialized(() -> log.info("节点监听器初始化完成，监听: {}", watchPath))
                .build();

        curatorCache.listenable().addListener(listener);

        log.info("已创建服务监听");
    }

    private String parseServiceName(ChildData childData){
        String s = new String(childData.getData());
        return s;
    }

    // 处理节点创建事件
    private void handleNodeCreated(ChildData childData) {
        if (!isDirectChild(childData.getPath(), currentWatchPath)) return;
        updateServiceCache(currentWatchPath);
        log.info("服务节点已创建: {}", childData.getPath());
    }

    // 处理节点更新事件
    private void handleNodeUpdated(ChildData oldData, ChildData newData) {
        if (!isDirectChild(oldData.getPath(), currentWatchPath)) return;
        updateServiceCache(currentWatchPath);
        log.debug("服务节点已更新: {}", oldData.getPath());
    }

    // 处理节点删除事件
    private void handleNodeDeleted(ChildData childData) {
        if (!isDirectChild(childData.getPath(), currentWatchPath)) return;

        updateServiceCache(currentWatchPath);
        log.debug("服务节点已下线: {}", childData.getPath());
    }

    // 更新本地缓存
    private void updateServiceCache(String servicePath) {
        try {
            List<String> instances = client.getChildren().forPath(servicePath);
            cache.addServiceList(servicePath, instances);
        } catch (Exception e) {
            log.error("服务节点缓存更新失败: {}", servicePath, e);
        }
    }

    // 判断是否为直接子节点（避免孙子节点干扰）
    public boolean isDirectChild(String fullPath, String parentPath) {
        return fullPath.startsWith(parentPath) &&
                fullPath.substring(parentPath.length()).lastIndexOf('/') <= 0;
    }
}
