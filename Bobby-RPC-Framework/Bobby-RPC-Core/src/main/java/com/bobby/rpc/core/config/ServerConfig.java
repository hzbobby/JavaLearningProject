package com.bobby.rpc.core.config;

import com.bobby.rpc.core.config.properties.NettyProperties;
import com.bobby.rpc.core.server.rpcServer.IRpcServer;
import com.bobby.rpc.core.server.rpcServer.impl.NettyRPCServer;
import com.bobby.rpc.core.server.provider.ServiceProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NettyProperties.class)
public class ServerConfig {

    @Bean
    public IRpcServer rpcServer(ServiceProvider serviceProvider, NettyProperties nettyProperties) {
        NettyRPCServer nettyRPCServer = new NettyRPCServer(serviceProvider);
        nettyRPCServer.start(nettyProperties.getPort());
        return nettyRPCServer;
    }
}
