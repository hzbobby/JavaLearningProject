package com.bobby.rpc.v9.blog.controller;

import com.bobby.rpc.v9.sample.domain.Blog;
import com.bobby.rpc.v9.sample.service.IBlogService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/4/6
 */
@RestController
@RequestMapping("/blog")
public class BlogController {
    @Resource
    private IBlogService blogService;

    @PostMapping("/addBlog")
    public String addBlog(Blog blog){
        blogService.addBlog(blog);
        return "success";
    }
}
