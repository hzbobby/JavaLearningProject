package com.bobby.rpc.core.client;

import com.bobby.rpc.core.client.circuitBreaker.CircuitBreakerProvider;
import com.bobby.rpc.core.client.discover.IServiceDiscover;
import com.bobby.rpc.core.client.discover.impl.ZkServiceDiscover;
import com.bobby.rpc.core.client.loadbalance.RoundLoadBalance;
import com.bobby.rpc.core.client.proxy.ClientProxy;
import com.bobby.rpc.core.client.rpcClient.IRpcClient;
import com.bobby.rpc.core.client.rpcClient.impl.NettyRpcClient;
import com.bobby.rpc.core.common.constants.ZkConstants;
import com.bobby.rpc.core.sample.IDemoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

@Slf4j
public class TestClient {
    public static void main(String[] args) throws InterruptedException {

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(
                3000,
                3
        );

        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("192.168.160.128:2181")   // zk 服务地址 host:port
                .sessionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .namespace(ZkConstants.ZK_NAMESPACE)
                .build();

        IServiceDiscover serviceDiscover = new ZkServiceDiscover(client, new RoundLoadBalance());
        CircuitBreakerProvider circuitBreakerProvider = new CircuitBreakerProvider();

        IRpcClient rpcClient=new NettyRpcClient(serviceDiscover);

        ClientProxy clientProxy=new ClientProxy(rpcClient, serviceDiscover, circuitBreakerProvider);

        IDemoService proxy = clientProxy.createProxy(IDemoService.class);

        for(int i = 0; i < 100; i++) {
            new Thread(()->{
                try{
                    System.out.println(proxy.sayHello("ProxyClient"));
                } catch (Exception e){
                    System.out.println("服务失败");
                    e.printStackTrace();
                }
            }).start();
        }
        System.out.println("执行完毕");

    }
}
