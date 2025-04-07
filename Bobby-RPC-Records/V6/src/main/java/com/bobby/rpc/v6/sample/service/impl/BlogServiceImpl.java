package com.bobby.rpc.v6.sample.service.impl;

import com.bobby.rpc.v6.sample.domain.Blog;
import com.bobby.rpc.v6.sample.service.IBlogService;

public class BlogServiceImpl implements IBlogService {
    @Override
    public Blog getBlogById(Long id) {
        Blog blog = Blog.builder().id(id).title("我的博客").useId(22L).build();
        System.out.println("客户端查询了" + id + "博客");
        return blog;
    }

    @Override
    public void addBlog(Blog blog) {
        System.out.println("插入的 Blog 为："+ blog.toString());
    }
}