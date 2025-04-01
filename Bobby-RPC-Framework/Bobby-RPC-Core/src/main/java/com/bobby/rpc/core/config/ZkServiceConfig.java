package com.bobby.rpc.core.config;

import com.bobby.rpc.core.common.loadbalance.ILoadBalance;
import com.bobby.rpc.core.common.loadbalance.RoundLoadBalance;
import com.bobby.rpc.core.config.properties.BRpcProperties;
import com.bobby.rpc.core.config.properties.NettyProperties;
import com.bobby.rpc.core.config.properties.ZkProperties;
import com.bobby.rpc.core.prosessor.RpcServiceProcessor;
import com.bobby.rpc.core.register.IServiceRegister;
import com.bobby.rpc.core.register.ZkServiceRegister;
import com.bobby.rpc.core.server.ServiceProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.autoconfigure.web.ServerProperties;
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
@EnableConfigurationProperties({ZkProperties.class, BRpcProperties.class, NettyProperties.class})
@RequiredArgsConstructor
public class ZkServiceConfig {
    private final ServerProperties serverProperties;

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

    @Bean
    public IServiceRegister serviceRegister(BRpcProperties rpcProperties, ILoadBalance loadBalance, CuratorFramework client) {
        return new ZkServiceRegister(rpcProperties, loadBalance, client);
    }


    @Bean
    public ServiceProvider serviceProvider(IServiceRegister serviceRegister, NettyProperties nettyProperties) {
        // 这里统一注册成 netty 的端口
        // 本机 ip + netty 端口
        return new ServiceProvider(serviceRegister, serverProperties.getAddress().getHostAddress(), nettyProperties.getPort());
    }

    @Bean
    public RpcServiceProcessor rpcServiceProcessor(ServiceProvider serviceProvider) {
        return new RpcServiceProcessor(serviceProvider);
    }

}
