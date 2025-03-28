package com.bobby.myrpc.version2.client;

import com.bobby.myrpc.version2.domain.Blog;
import com.bobby.myrpc.version2.domain.User;
import com.bobby.myrpc.version2.service.IBlogService;
import com.bobby.myrpc.version2.service.IUserService;

import java.util.Random;

public class RPCClientMain {
    public static void main(String[] args) {
        // 使用代理类
        ClientProxy clientProxy = new ClientProxy("127.0.0.1", 8899);
        IUserService proxyService = clientProxy.getProxy(IUserService.class);

        User user = proxyService.getUserById(new Random().nextLong());
        System.out.println(user);

        // 调用其他方法
        IBlogService blogService = clientProxy.getProxy(IBlogService.class);
        Blog blog = blogService.getBlogById(222);
        System.out.println(blog);
    }
}