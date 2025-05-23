package com.bobby.rpc.v9.sample.service;

import com.bobby.rpc.v9.sample.domain.Blog;
import com.bobby.rpc.v9.sample.domain.User;

public interface IUserService {

    public User getUser(Long id);

    public Blog getBlog(Long id);
}
