package com.bobby.myrpc.version9.config;

import com.bobby.rpc.core.config.properties.NettyProperties;
import com.bobby.rpc.core.server.provider.ServiceProvider;
import com.bobby.rpc.core.server.rpcServer.IRpcServer;
import com.bobby.rpc.core.server.rpcServer.impl.NettyRpcServer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NettyProperties.class)
public class ServerConfig {

    @Bean
    public IRpcServer rpcServer(ServiceProvider serviceProvider, NettyProperties nettyProperties) {
        NettyRpcServer nettyRPCServer = new NettyRpcServer(serviceProvider);
        nettyRPCServer.start(nettyProperties.getPort());
        return nettyRPCServer;
    }
}
