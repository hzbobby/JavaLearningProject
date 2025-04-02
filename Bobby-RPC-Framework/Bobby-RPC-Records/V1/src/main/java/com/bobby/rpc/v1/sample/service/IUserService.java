package com.bobby.rpc.v1.sample.service;

import com.bobby.rpc.v1.sample.domain.User;

public interface IUserService {

    public User getUser(Long id);
}
