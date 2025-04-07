package com.bobby.rpc.v5.sample.service;

import com.bobby.rpc.v5.sample.domain.Blog;

public interface IBlogService {
    public Blog getBlogById(Long id);
    public void addBlog(Blog blog);
}
