package com.bobby.rpc.v9.user.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.bobby.rpc.v9.starter.sample.domain.User;
import com.bobby.rpc.v9.starter.sample.service.IUserService;
import org.springframework.stereotype.Service;

@Service
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
