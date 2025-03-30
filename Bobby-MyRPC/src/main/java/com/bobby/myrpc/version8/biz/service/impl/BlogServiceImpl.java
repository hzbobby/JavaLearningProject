package com.bobby.myrpc.version8.biz.service.impl;

import com.bobby.myrpc.version8.biz.domain.Blog;
import com.bobby.myrpc.version8.biz.service.IBlogService;
import com.bobby.myrpc.version8.biz.service.IUserService;
import com.bobby.myrpc.version8.common.annotation.RpcReference;
import com.bobby.myrpc.version8.common.annotation.RpcService;
import org.springframework.stereotype.Service;

@RpcService
@Service
public class BlogServiceImpl implements IBlogService {
    @RpcReference(interfaceClass = IUserService.class)
    IUserService userService;

    @Override
    public Blog getBlogById(Integer id) {
        Blog blog = Blog.builder().id(id).title("我的博客").useId(22).build();
        System.out.println("客户端查询了" + id + "博客");
        return blog;
    }
}