package com.bobby.rpc.consumer;

import com.bobby.rpc.core.config.properties.BRpcProperties;
import com.bobby.rpc.core.config.properties.ZkProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/30
 */
@SpringBootApplication
@EnableConfigurationProperties({BRpcProperties.class, ZkProperties.class})
@ComponentScan(basePackages = {
        "com.bobby.rpc.consumer",
        "com.bobby.rpc.core"
})
public class ConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}
