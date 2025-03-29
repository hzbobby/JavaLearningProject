package com.bobby.myrpc.version8;

import com.bobby.myrpc.version8.config.ZkProperties;
import com.bobby.myrpc.version8.register.ZkServiceRegister;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.InetSocketAddress;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class ZkServiceRegisterTest {
    ZkServiceRegister zkServiceRegister;

    @BeforeEach
    void setUp() throws Exception {
        ZkProperties zkProperties = ZkProperties.builder()
                .address("192.168.160.128:2181")
                .namespace("TEST_MYRPC")
                .sessionTimeoutMs(100000)
                .retry(
                        ZkProperties.Retry.builder()
                                .maxRetries(3)
                                .baseSleepTimeMs(1000)
                                .build()
                ).build();

        zkServiceRegister = new ZkServiceRegister("test-app", zkProperties);
    }

    @AfterEach
    void tearDown() throws Exception {
        zkServiceRegister.close();
    }

    String serviceName = "TestService";
    String host = "127.0.0.1";
    int port = 8899;

    void register(
            String serviceName
    ) {
        zkServiceRegister.register(serviceName, new InetSocketAddress(host, port));
    }

    InetSocketAddress discovery(String serviceName) {
        return zkServiceRegister.serviceDiscovery(serviceName);
    }

    @Test
    void testRegister() {
        register(serviceName);
    }

    @Test
    void testServiceDiscovery() {
        testRegister();
        InetSocketAddress inetSocketAddress = discovery(serviceName);
        log.info("++++ testServiceDiscovery: " + inetSocketAddress.toString());
    }

    @Test
    void testMultiRegister() {
        String s1 = "s1";
        String s2 = "s2";
        String s3 = "s3";
        register(s1);
        register(s2);
        register(s3);

        log.info("++++ testMultiRegister: {}", discovery(s3));
        log.info("++++ testMultiRegister: {}", discovery(s2));
        log.info("++++ testMultiRegister: {}", discovery(s1));
    }

    @Test
    void testMultiInstance() {
        String s1 = "s1";
        InetSocketAddress sa0 = new InetSocketAddress(host, 8890);
        InetSocketAddress sa1 = new InetSocketAddress(host, 8891);
        InetSocketAddress sa2 = new InetSocketAddress(host, 8892);
        InetSocketAddress sa3 = new InetSocketAddress(host, 8893);
        zkServiceRegister.register(s1, sa0);
        zkServiceRegister.register(s1, sa1);
        zkServiceRegister.register(s1, sa2);
        zkServiceRegister.register(s1, sa3);

        // 轮询的方式
        log.info("++++ testMultiInstance: {}", discovery(s1));
        log.info("++++ testMultiInstance: {}", discovery(s1));
        log.info("++++ testMultiInstance: {}", discovery(s1));
        log.info("++++ testMultiInstance: {}", discovery(s1));
    }

    @Test
    void testWatch() {
        String s1 = "s1";
        InetSocketAddress sa0 = new InetSocketAddress(host, 8890);
        InetSocketAddress sa1 = new InetSocketAddress(host, 8891);
        InetSocketAddress sa2 = new InetSocketAddress(host, 8892);
        zkServiceRegister.register(s1, sa0);
        zkServiceRegister.watch(s1);
        // 增加实例
        zkServiceRegister.register(s1, sa1);
        zkServiceRegister.register(s1, sa2);
        // 删除实例
        zkServiceRegister.remove(s1, sa0);
        zkServiceRegister.register(s1, sa0);
    }

}