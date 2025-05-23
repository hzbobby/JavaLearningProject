package com.bobby.myrpc.version7.register;

import java.net.InetSocketAddress;

// 服务注册接口，两大基本功能，注册：保存服务与地址。 查询：根据服务名查找地址
public interface IServiceRegister {
    void register(String serviceName, InetSocketAddress serverAddress);
    InetSocketAddress serviceDiscovery(String serviceName);

    void createWatch(String path);
}