package com.bobby.myrpc.version5.client;

import com.bobby.myrpc.version5.domain.Blog;
import com.bobby.myrpc.version5.domain.User;
import com.bobby.myrpc.version5.service.IBlogService;
import com.bobby.myrpc.version5.service.IUserService;

public class RPCClientMain {
    public static void main(String[] args) {
        // 构建一个使用java Socket传输的客户端
        IRPCClient rpcClient = new NettyRPCClient("127.0.0.1", 8899);
        // 把这个客户端传入代理客户端
        RPCClientProxy rpcClientProxy = new RPCClientProxy(rpcClient);
        // 代理客户端根据不同的服务，获得一个代理类， 并且这个代理类的方法以或者增强（封装数据，发送请求）
        IUserService userService = rpcClientProxy.getProxy(IUserService.class);
        // 调用方法
        User user = userService.getUserById(10L);
        System.out.println(user);

        IBlogService blogService = rpcClientProxy.getProxy(IBlogService.class);
        Blog blog = blogService.getBlogById(234);
        System.out.println(blog);
    }
}