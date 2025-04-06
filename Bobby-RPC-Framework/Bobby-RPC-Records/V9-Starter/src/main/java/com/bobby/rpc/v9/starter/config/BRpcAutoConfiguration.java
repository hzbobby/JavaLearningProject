package com.bobby.rpc.v9.starter.config;

import com.bobby.rpc.v9.client.circuitBreaker.CircuitBreakerProvider;
import com.bobby.rpc.v9.client.discover.IServiceDiscover;
import com.bobby.rpc.v9.client.discover.impl.ZkServiceDiscover;
import com.bobby.rpc.v9.client.proxy.ClientProxy;
import com.bobby.rpc.v9.client.rpcClient.IRpcClient;
import com.bobby.rpc.v9.client.rpcClient.impl.NettyRpcClient;
import com.bobby.rpc.v9.common.loadbalance.ILoadBalance;
import com.bobby.rpc.v9.common.loadbalance.RoundLoadBalance;
import com.bobby.rpc.v9.server.provider.ServiceProvider;
import com.bobby.rpc.v9.server.ratelimit.provider.RateLimitProvider;
import com.bobby.rpc.v9.server.register.IServiceRegister;
import com.bobby.rpc.v9.server.register.impl.ZkServiceRegister;
import com.bobby.rpc.v9.server.rpcServer.IRpcServer;
import com.bobby.rpc.v9.server.rpcServer.impl.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/4/6
 */
@EnableConfigurationProperties(value = {BRpcProperties.class})
@Configuration
@Slf4j
public class BRpcAutoConfiguration {
    // 在这个配置项里面，创建相关的 bean 对象

    private final BRpcProperties brpcProperties;
    public BRpcAutoConfiguration(BRpcProperties brpcProperties) {
        this.brpcProperties = brpcProperties;
    }

    // zk client
    @Bean
    public CuratorFramework zkClient(){
        log.info("Create bean of CuratorFramework zkClient");
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(
                brpcProperties.getZk().getRetry().getMaxRetries(),
                brpcProperties.getZk().getRetry().getMaxRetries()
        );

        try (CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(brpcProperties.getZk().getAddress())   // zk 服务地址 host:port
//                .connectString("192.168.160.128:2181")   // zk 服务地址 host:port
                .sessionTimeoutMs(brpcProperties.getZk().getSessionTimeoutMs())
                .retryPolicy(retryPolicy)
                .namespace(brpcProperties.getZk().getNamespace())
                .build()) {
            return client;
        }catch (Exception e){
            log.error("zk client create error", e);
            throw new RuntimeException("zk client create error", e);
        }
    }

    @Bean
    public IServiceRegister serviceRegister(CuratorFramework client){
        log.info("Create bean of IServiceRegister serviceRegister");
        return new ZkServiceRegister(client);
    }

    @Bean
    public RateLimitProvider rateLimitProvider(){
        log.info("Create bean of RateLimitProvider rateLimitProvider");
        return new RateLimitProvider();
    }

    @Bean
    public ServiceProvider serviceProvider(IServiceRegister serviceRegister, RateLimitProvider rateLimitProvider){
        log.info("Create bean of ServiceProvider serviceProvider");

        // 本机 ip + 指定 netty 通信的端口
        return new ServiceProvider(serviceRegister, new InetSocketAddress("127.0.0.1", brpcProperties.getNetty().getPort()), rateLimitProvider);
    }

    @Bean
    public IRpcServer rpcServer(ServiceProvider serviceProvider){
        log.info("Create bean of IRpcServer rpcServer");

        NettyRpcServer nettyRpcServer = new NettyRpcServer(serviceProvider);
        nettyRpcServer.start(brpcProperties.getNetty().getPort());
        return nettyRpcServer;
    }

    // Client

    @Bean
    public ILoadBalance loadBalance(){
        log.info("Create bean of ILoadBalance loadBalance");

        return new RoundLoadBalance();
    }

    @Bean
    public IServiceDiscover serviceDiscover(CuratorFramework client, ILoadBalance loadBalance){
        log.info("Create bean of IServiceDiscover serviceDiscover");

        return new ZkServiceDiscover(client, loadBalance);
    }

    @Bean
    public CircuitBreakerProvider circuitBreakerProvider(){
        log.info("Create bean of CircuitBreakerProvider circuitBreakerProvider");

        return new CircuitBreakerProvider();
    }

    @Bean
    public IRpcClient rpcClient(IServiceDiscover serviceDiscover){
        log.info("Create bean of IRpcClient rpcClient");

        return new NettyRpcClient(serviceDiscover);
    }

    @Bean
    public ClientProxy clientProxy(IRpcClient rpcClient, CircuitBreakerProvider circuitBreakerProvider, IServiceDiscover serviceDiscover ){
        log.info("Create bean of ClientProxy clientProxy");

        return new ClientProxy(rpcClient, circuitBreakerProvider, serviceDiscover);
    }
}
