package com.bobby.myrpc.version8.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/30
 */

@Data
@ConfigurationProperties(prefix = "brpc.netty")
public class NettyProperties {
    int port;
}
