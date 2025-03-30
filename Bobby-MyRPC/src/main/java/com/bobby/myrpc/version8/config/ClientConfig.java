package com.bobby.myrpc.version8.config;

import com.bobby.myrpc.version8.client.IRpcClient;
import com.bobby.myrpc.version8.client.NettyRPCClient;
import com.bobby.myrpc.version8.client.RpcClientProxy;
import com.bobby.myrpc.version8.register.IServiceRegister;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/30
 */
@Configuration
public class ClientConfig {

    @Bean
    public IRpcClient rpcClient(IServiceRegister serviceRegister) {
        return new NettyRPCClient(serviceRegister);
    }

    @Bean
    public RpcClientProxy rpcClientProxy(IRpcClient rpcClient) {
        return new RpcClientProxy(rpcClient);
    }
}
