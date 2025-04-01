package com.bobby.myrpc.version9.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
