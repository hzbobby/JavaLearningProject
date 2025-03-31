package com.bobby.myrpc.version1.service;

import com.bobby.myrpc.version1.domain.User;

public interface IUserService {
    User getUserById(Long id);
}
