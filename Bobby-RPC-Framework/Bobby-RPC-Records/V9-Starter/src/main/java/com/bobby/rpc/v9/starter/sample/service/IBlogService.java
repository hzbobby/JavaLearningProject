package com.bobby.rpc.v9.starter.sample.service;

import com.bobby.rpc.v9.starter.sample.domain.Blog;

public interface IBlogService {
    public Blog getBlogById(Long id);
    public void addBlog(Blog blog);
}
