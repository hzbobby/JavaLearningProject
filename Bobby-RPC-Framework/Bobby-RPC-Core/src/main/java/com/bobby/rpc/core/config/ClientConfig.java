package com.bobby.rpc.core.config;


import com.bobby.rpc.core.client.IRpcClient;
import com.bobby.rpc.core.client.NettyRpcClient;
import com.bobby.rpc.core.factory.InvokeHandler;
import com.bobby.rpc.core.prosessor.RpcReferenceProcessor;
import com.bobby.rpc.core.register.IServiceRegister;
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
