package com.bobby.rpc.core.config;


import com.bobby.rpc.core.client.discover.IServiceDiscover;
import com.bobby.rpc.core.client.rpcClient.impl.NettyRpcClient;
import com.bobby.rpc.core.client.rpcClient.IRpcClient;
import com.bobby.rpc.core.client.proxy.InvokeHandler;
import com.bobby.rpc.core.prosessor.RpcReferenceProcessor;
import com.bobby.rpc.core.server.register.IServiceRegister;
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
    public IRpcClient rpcClient(IServiceDiscover serviceDiscover) {
        NettyRpcClient nettyRpcClient = new NettyRpcClient(serviceDiscover);
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
