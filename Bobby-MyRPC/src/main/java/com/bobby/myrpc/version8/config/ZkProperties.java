package com.bobby.myrpc.version8.config;

import lombok.Builder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.beans.ConstructorProperties;

/**
 * 参考 dubbo 通常会用到
 * registry:
 * address: zookeeper://192.168.160.128:2181
 */
@Builder
@Data
@ConfigurationProperties(prefix = "myrpc.zk")  // 使用小写字母和中划线风格
public class ZkProperties {
    private String address;  // 对应 yaml 中的 myrpc.zk.address
    private int sessionTimeoutMs;
    private String namespace;
    private Retry retry;

    @Data
    @Builder
    public static class Retry {
        private int maxRetries;
        private int baseSleepTimeMs;
    }
}
