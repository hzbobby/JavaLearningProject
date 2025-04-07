package com.bobby.rpc.v9.sample.service;

import com.bobby.rpc.v9.sample.domain.Blog;

public interface IBlogService {
    public Blog getBlogById(Long id);
    public void addBlog(Blog blog);
}
