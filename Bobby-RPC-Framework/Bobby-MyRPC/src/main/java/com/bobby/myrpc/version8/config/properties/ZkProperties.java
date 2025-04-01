package com.bobby.myrpc.version8.config.properties;

import lombok.Builder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/30
 */
@Data
@Builder
@ConfigurationProperties(prefix = "brpc.zk")
public class ZkProperties {
    private String address;  // 直接映射 myrpc.zk.address
    private int sessionTimeoutMs;  // 自动绑定 session-timeout-ms
    private String namespace;
    private Retry retry;    // 嵌套对象

    @Data
    @Builder
    public static class Retry {
        private int maxRetries;      // 绑定 max-retries
        private int baseSleepTimeMs; // 绑定 base-sleep-time-ms
    }
}
