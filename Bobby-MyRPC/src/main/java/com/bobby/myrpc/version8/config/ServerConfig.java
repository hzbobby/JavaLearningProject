package com.bobby.myrpc.version8.config;

import com.bobby.myrpc.version8.register.IServiceRegister;
import com.bobby.myrpc.version8.server.IRPCServer;
import com.bobby.myrpc.version8.server.NettyRPCServer;
import com.bobby.myrpc.version8.server.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/30
 */
@RequiredArgsConstructor
@Configuration
public class ServerConfig {
    private final ServerProperties serverProperties;

    @Bean
    public ServiceProvider serviceProvider(IServiceRegister serviceRegister) {
        return new ServiceProvider(serviceRegister,serverProperties.getAddress().getHostAddress(), serverProperties.getPort());
    }

    @Bean
    public IRPCServer rpcServer(ServiceProvider serviceProvider) {
        NettyRPCServer nettyRPCServer = new NettyRPCServer(serviceProvider);
        nettyRPCServer.start(serverProperties.getPort());
//        nettyRPCServer.start(8899);
        return nettyRPCServer;
    }
}
