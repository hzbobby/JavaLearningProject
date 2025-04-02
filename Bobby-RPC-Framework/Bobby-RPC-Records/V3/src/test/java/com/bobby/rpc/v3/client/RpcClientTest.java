package com.bobby.rpc.v3.client;

import cn.hutool.core.util.RandomUtil;
import com.bobby.rpc.v3.client.proxy.ClientProxy;
import com.bobby.rpc.v3.sample.domain.Blog;
import com.bobby.rpc.v3.sample.domain.User;
import com.bobby.rpc.v3.sample.service.IBlogService;
import com.bobby.rpc.v3.sample.service.IUserService;

public class RpcClientTest {
    public static void main(String[] args) {
        // 使用代理类
        ClientProxy clientProxy = new ClientProxy("127.0.0.1", 8899);
        IUserService userService = clientProxy.createProxy(IUserService.class);

        IBlogService blogService = clientProxy.createProxy(IBlogService.class);


        for(int i=0; i<100; i++){
            User user = userService.getUser(RandomUtil.randomLong());
            System.out.println("接受的User: "+ user);

            blogService.addBlog(Blog.builder()
                    .id(RandomUtil.randomLong())
                    .title(RandomUtil.randomString(18))
                    .useId(RandomUtil.randomLong())
                    .build());
        }
    }
}
