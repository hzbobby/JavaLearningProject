package com.bobby.myrpc.version8.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "myrpc.application")
public class MyRPCProperties {
    private String name;
}
