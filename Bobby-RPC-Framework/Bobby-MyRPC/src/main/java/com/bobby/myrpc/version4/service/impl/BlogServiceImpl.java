package com.bobby.myrpc.version4.service.impl;

import com.bobby.myrpc.version4.domain.Blog;
import com.bobby.myrpc.version4.service.IBlogService;

public class BlogServiceImpl implements IBlogService {

    @Override
    public Blog getBlogById(Integer id) {
        Blog blog = Blog.builder().id(id).title("我的博客").useId(22).build();
        System.out.println("客户端查询了" + id + "博客");
        return blog;
    }
}