package com.bobby.rpc.core.config;

import com.bobby.rpc.core.server.IRpcServer;
import com.bobby.rpc.core.server.NettyRPCServer;
import com.bobby.rpc.core.server.ServiceProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NettyProperties.class)
public class ServerConfig {

    @Bean
    public IRpcServer rpcServer(ServiceProvider serviceProvider, NettyProperties nettyProperties) {
        NettyRPCServer nettyRPCServer = new NettyRPCServer(serviceProvider);
//        nettyRPCServer.start(serverProperties.getPort());
        nettyRPCServer.start(nettyProperties.getPort());
        return nettyRPCServer;
    }
}
