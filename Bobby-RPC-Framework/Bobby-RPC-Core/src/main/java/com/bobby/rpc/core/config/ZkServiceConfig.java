package com.bobby.rpc.core.config;

import com.bobby.myrpc.version8.common.loadbalance.ILoadBalance;
import com.bobby.myrpc.version8.common.loadbalance.RoundLoadBalance;
import com.bobby.myrpc.version8.config.ZkProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/30
 * 创建 zk 客户端
 */
@Slf4j
@Component
@EnableConfigurationProperties(ZkProperties.class)
public class ZkServiceConfig {

    @Bean
    public CuratorFramework curatorFramework(ZkProperties zkProperties) {
        log.info("初始化 ZooKeeper 客户端");
        // 使用配置中的参数
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(
                zkProperties.getRetry().getBaseSleepTimeMs(),
                zkProperties.getRetry().getMaxRetries()
        );

        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(zkProperties.getAddress())   // zk 服务地址 host:port
                .sessionTimeoutMs(zkProperties.getSessionTimeoutMs())
                .retryPolicy(retryPolicy)
                .namespace(zkProperties.getNamespace())
                .build();

        return client;
    }

    @Bean
    public ILoadBalance zkLoadBalance() {
        return new RoundLoadBalance();
    }

}
