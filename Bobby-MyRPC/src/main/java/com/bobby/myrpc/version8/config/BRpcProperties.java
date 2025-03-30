package com.bobby.myrpc.version8.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/30
 */

@Data
@ConfigurationProperties(prefix = "brpc")
public class BRpcProperties {
    String applicationName;
    Boolean watch;
}
