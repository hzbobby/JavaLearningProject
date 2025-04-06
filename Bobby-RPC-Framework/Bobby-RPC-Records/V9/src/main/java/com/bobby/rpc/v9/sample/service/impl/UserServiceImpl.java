package com.bobby.rpc.v9.sample.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.bobby.rpc.v9.sample.domain.User;
import com.bobby.rpc.v9.sample.service.IUserService;

public class UserServiceImpl implements IUserService {
    @Override
    public User getUser(Long id) {
        return User.builder()
                .id(RandomUtil.randomLong())
                .name(RandomUtil.randomString(10))
                .age(RandomUtil.randomInt())
                .build();
    }
}
