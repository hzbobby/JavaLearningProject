package com.bobby.rpc.v9.user.controller;

import com.bobby.rpc.v9.sample.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/4/6
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private IUserService userService;

    @PostMapping("/getUser/{id}")
    public String getUser(@PathVariable("id") Long id) {
        return userService.getUser(id).toString();
    }

    @PostMapping("/getBlog/{id}")
    public String getBlog(@PathVariable("id") Long id) {
        return userService.getBlog(id).toString();
    }
}
