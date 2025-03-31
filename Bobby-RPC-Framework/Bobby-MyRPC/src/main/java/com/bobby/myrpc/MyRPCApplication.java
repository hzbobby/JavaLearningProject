package com.bobby.myrpc;

import com.bobby.myrpc.version8.config.BRpcProperties;
import com.bobby.myrpc.version8.config.NettyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

//@ServiceScan(basePackages = "com.bobby.myrpc.version8.service")
@EnableConfigurationProperties({BRpcProperties.class, NettyProperties.class}) //
@SpringBootApplication
public class MyRPCApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyRPCApplication.class, args);
    }
}
