package com.bobby.rpc.v8.client;

import cn.hutool.core.util.RandomUtil;
import com.bobby.rpc.v8.client.circuitBreaker.CircuitBreakerProvider;
import com.bobby.rpc.v8.client.discover.IServiceDiscover;
import com.bobby.rpc.v8.client.discover.impl.ZkServiceDiscover;
import com.bobby.rpc.v8.client.proxy.ClientProxy;
import com.bobby.rpc.v8.client.rpcClient.IRpcClient;
import com.bobby.rpc.v8.client.rpcClient.impl.NettyRpcClient;
import com.bobby.rpc.v8.common.loadbalance.ILoadBalance;
import com.bobby.rpc.v8.common.loadbalance.RoundLoadBalance;
import com.bobby.rpc.v8.sample.domain.Blog;
import com.bobby.rpc.v8.sample.domain.User;
import com.bobby.rpc.v8.sample.service.IBlogService;
import com.bobby.rpc.v8.sample.service.IUserService;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class RpcClientTest {
    public static void main(String[] args) throws InterruptedException {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(
                1000,
                3
        );

        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("192.168.232.129:2181")   // zk 服务地址 host:port
//                .connectString("192.168.160.128:2181")   // zk 服务地址 host:port
                .sessionTimeoutMs(30000)
                .retryPolicy(retryPolicy)
                .namespace("BobbyRPC")
                .build();

        ILoadBalance loadBalance = new RoundLoadBalance();

        IServiceDiscover serviceDiscover = new ZkServiceDiscover(client, loadBalance);

        CircuitBreakerProvider circuitBreakerProvider = new CircuitBreakerProvider();

//        IRpcClient rpcClient = new SimpleRpcClient("127.0.0.1", 8899);
        IRpcClient rpcClient = new NettyRpcClient(serviceDiscover);
        ClientProxy clientProxy = new ClientProxy(rpcClient, circuitBreakerProvider, serviceDiscover);
        IUserService userService = clientProxy.createProxy(IUserService.class);

        IBlogService blogService = clientProxy.createProxy(IBlogService.class);


        for(int i=1; i<=100; i++){
            if(i%30 == 0){
//                Thread.sleep(21000);
                ;
            }
            User user = userService.getUser(RandomUtil.randomLong());
            System.out.println("接受的User: "+ user);

            blogService.addBlog(Blog.builder()
                    .id(RandomUtil.randomLong())
                    .title(RandomUtil.randomString(18))
                    .useId(RandomUtil.randomLong())
                    .build());
        }

//        rpcClient.close();
    }
}
