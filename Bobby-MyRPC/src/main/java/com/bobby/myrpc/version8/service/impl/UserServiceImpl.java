package com.bobby.myrpc.version8.service.impl;

import com.bobby.myrpc.version8.annotation.RpcService;
import com.bobby.myrpc.version8.domain.User;
import com.bobby.myrpc.version8.service.IUserService;

import java.util.Random;
import java.util.UUID;

@RpcService
public class UserServiceImpl implements IUserService {

    @Override
    public User getUserById(Long id) {
        System.out.println("getUserById, id: " + id);
        // 模拟数据库，返回一个用户
        // 模拟从数据库中取用户的行为
        Random random = new Random();
        return User.builder().username(UUID.randomUUID().toString())
                .id(id)
                .age(random.nextInt()).build();
    }
}
