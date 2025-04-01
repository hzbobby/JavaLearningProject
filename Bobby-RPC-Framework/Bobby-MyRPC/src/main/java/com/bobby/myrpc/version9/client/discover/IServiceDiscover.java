package com.bobby.myrpc.version9.client.discover;

import java.net.InetSocketAddress;

// 服务发现
public interface IServiceDiscover {
    InetSocketAddress serviceDiscovery(String serviceName);

    boolean retryable(String serviceName);
}
