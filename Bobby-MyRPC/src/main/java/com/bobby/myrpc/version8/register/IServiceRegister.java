package com.bobby.myrpc.version8.register;

import java.net.InetSocketAddress;

// 服务注册接口，两大基本功能，注册：保存服务与地址。 查询：根据服务名查找地址
public interface IServiceRegister {
    /**
     *
     * @param serviceName 服务名称
     * @param serverAddress 服务地址
     */
    void register(String serviceName, InetSocketAddress serverAddress);

    /**
     * 删除实例
     * @param serviceName 服务名称
     * @param serverAddress 服务地址
     */
    void remove(String serviceName, InetSocketAddress serverAddress);


    /**
     * 根据服务名返回地址
     * @param serviceName 服务名称
     * @return InetSocketAddress
     */
    InetSocketAddress serviceDiscovery(String serviceName);

    /**
     * 创建一个监听
     * 例如监听到某个实例变化了，可以更新本地缓存
     * @param serviceName 实例地址
     */
    void watch(String serviceName);

    /**
     * 关闭 zk
     */
    void close();
}