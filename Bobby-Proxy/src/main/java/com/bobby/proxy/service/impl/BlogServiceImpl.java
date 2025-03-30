package com.bobby.proxy.service.impl;

import com.bobby.proxy.service.IBlogService;
import com.bobby.proxy.service.IUserService;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/30
 */
public class BlogServiceImpl implements IBlogService {
    IUserService userService;

    @Override
    public void setUserService(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public void doSomething() {
        System.out.println("BlogService do something, IUserService userService: ");
        userService.doSomething();
    }
}
