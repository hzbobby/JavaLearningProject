package com.bobby.myrpc.version8.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/30
 */

@ConfigurationProperties(prefix = "brpc.netty")
public class NettyProperties {
    int port;

    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
}
