package com.bobby.myrpc.version8.client;

import com.bobby.myrpc.version8.domain.Blog;
import com.bobby.myrpc.version8.domain.User;
import com.bobby.myrpc.version8.service.IBlogService;
import com.bobby.myrpc.version8.service.IUserService;

public class RPCClientMain {
    public static void main(String[] args) {
        // 构建一个使用java Socket/ netty/....传输的客户端
        IRPCClient rpcClient = new NettyRPCClient();
        // 把这个客户端传入代理客户端
        RPCClientProxy rpcClientProxy = new RPCClientProxy(rpcClient);
        // 代理客户端根据不同的服务，获得一个代理类， 并且这个代理类的方法以或者增强（封装数据，发送请求）
        IUserService userService = rpcClientProxy.getProxy(IUserService.class);
        User userByUserId = userService.getUserById(10L);
        System.out.println("从服务端得到的user为：" + userByUserId);

        IBlogService blogService = rpcClientProxy.getProxy(IBlogService.class);
        Blog blogById = blogService.getBlogById(10000);
        System.out.println("从服务端得到的blog为：" + blogById);
    }
}