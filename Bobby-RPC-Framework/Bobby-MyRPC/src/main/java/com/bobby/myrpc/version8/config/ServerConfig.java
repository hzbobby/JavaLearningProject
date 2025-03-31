package com.bobby.myrpc.version8.config;

import com.bobby.myrpc.version8.server.IRpcServer;
import com.bobby.myrpc.version8.server.NettyRPCServer;
import com.bobby.myrpc.version8.server.ServiceProvider;
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
