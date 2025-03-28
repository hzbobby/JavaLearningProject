package com.bobby.myrpc.version2.service;

import com.bobby.myrpc.version2.domain.User;

public interface IUserService {
    User getUserById(Long id);
}
