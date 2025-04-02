package com.bobby.rpc.core.config.properties;

import lombok.Builder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/30
 */
@Builder
@Data
@ConfigurationProperties(prefix = "brpc")
public class BRpcProperties {
    String applicationName;
    Boolean watch;
    NettyProperties netty;
    ZkProperties zk;

    @Data
    @Builder
    public static class NettyProperties{
        private int port;
        private String serializer;
    }

    @Data
    @Builder
    public static class ZkProperties {
        private String address;  // 直接映射 myrpc.zk.address
        private int sessionTimeoutMs;  // 自动绑定 session-timeout-ms
        private String namespace;
        private RetryProperties retry;    // 嵌套对象
    }

    @Data
    @Builder
    public static class RetryProperties {
        private int maxRetries;      // 绑定 max-retries
        private int baseSleepTimeMs; // 绑定 base-sleep-time-ms
    }

    public static BRpcProperties defaultProperties() {
        return BRpcProperties.builder()
                .applicationName("Bobby-App")
                .watch(true)
                .netty(NettyProperties.builder()
                        .serializer("json")
                        .port(8899)
                        .build())
                .zk(ZkProperties.builder()
                        .address("127.0.0.1")
                        .sessionTimeoutMs(10000)
                        .namespace("BobbyRPC")
                        .retry(RetryProperties
                                .builder()
                                .baseSleepTimeMs(1000)
                                .maxRetries(3)
                                .build())
                        .build())
                .build();
    }

}
