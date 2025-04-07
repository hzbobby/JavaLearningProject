package com.bobby.rpc.v7.sample.service;

import com.bobby.rpc.v7.sample.domain.Blog;

public interface IBlogService {
    public Blog getBlogById(Long id);
    public void addBlog(Blog blog);
}
