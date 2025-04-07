package com.bobby.rpc.v2.client;

import cn.hutool.core.util.RandomUtil;
import com.bobby.rpc.v2.client.proxy.ClientProxy;
import com.bobby.rpc.v2.sample.domain.User;
import com.bobby.rpc.v2.sample.service.IUserService;

public class RpcClientTest {
    public static void main(String[] args) {
        // 使用代理类
        ClientProxy clientProxy = new ClientProxy("127.0.0.1", 8899);
        IUserService proxyService = clientProxy.createProxy(IUserService.class);

        User user = proxyService.getUser(RandomUtil.randomLong());
        System.out.println("接受的User: "+ user);

        // 调用其他方法
    }
}
