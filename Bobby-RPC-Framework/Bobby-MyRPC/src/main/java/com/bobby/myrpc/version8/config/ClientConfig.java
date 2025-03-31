package com.bobby.myrpc.version8.config;


import com.bobby.myrpc.version8.client.IRpcClient;
import com.bobby.myrpc.version8.client.NettyRpcClient;
import com.bobby.myrpc.version8.factory.InvokeHandler;
import com.bobby.myrpc.version8.prosessor.RpcReferenceProcessor;
import com.bobby.myrpc.version8.register.IServiceRegister;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.InvocationHandler;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/30
 */
@Configuration
public class ClientConfig {
    @Bean
    public IRpcClient rpcClient(IServiceRegister serviceRegister) {
        NettyRpcClient nettyRpcClient = new NettyRpcClient(serviceRegister);
        return nettyRpcClient;
    }

    @Bean
    public InvocationHandler rpcClientInvocationHandler(IRpcClient rpcClient) {
        return new InvokeHandler(rpcClient);
    }

    @Bean
    public RpcReferenceProcessor rpcReferenceProcessor(InvocationHandler rpcClientInvocationHandler) {
        return new RpcReferenceProcessor(rpcClientInvocationHandler);
    }
}
