package com.bobby.rpc.provider;

import com.bobby.rpc.core.config.BRpcProperties;
import com.bobby.rpc.core.config.ZkProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/30
 */
@EnableConfigurationProperties({BRpcProperties.class, ZkProperties.class})
@SpringBootApplication
@ComponentScan(basePackages = {
        "com.bobby.rpc.provider",
        "com.bobby.rpc.core"
})
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}
