package com.bobby.rpc.v9.starter.sample.service;

import com.bobby.rpc.v9.starter.sample.domain.User;

public interface IUserService {

    public User getUser(Long id);
}
