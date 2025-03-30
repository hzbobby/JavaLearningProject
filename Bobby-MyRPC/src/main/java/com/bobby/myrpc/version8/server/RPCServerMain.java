package com.bobby.myrpc.version8.server;

import com.bobby.myrpc.version8.biz.service.IBlogService;
import com.bobby.myrpc.version8.biz.service.IUserService;
import com.bobby.myrpc.version8.biz.service.impl.BlogServiceImpl;
import com.bobby.myrpc.version8.biz.service.impl.UserServiceImpl;
import com.bobby.myrpc.version8.register.IServiceRegister;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;

/**
 * version 3: 引入 Netty
 */
public class RPCServerMain {

}