package com.bobby.rpc.v8.client.discover;

import java.net.InetSocketAddress;

// 服务发现
public interface IServiceDiscover {
    InetSocketAddress serviceDiscovery(String serviceName);
    // 服务级别的重试机制
    boolean retryable(String serviceName);
//
////    // 方法级别的重试机制
////    boolean retryable(String serviceName);
}
