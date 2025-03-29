package com.bobby.myrpc.version8.register;

import java.util.ArrayList;
import java.util.List;

// 服务收集器接口，用于收集 @RpcService 并用于延迟注册
public class ServiceCollector {
    // 比如我这个服务是提供者，那么我只需要把服务类的名称/class 记录下来
    // 服务地址是本 springboot 应用的 host:port
    // 因此这里只需要记录 class
    // 接口 -> 到实例
    private final List<Class<?>> serviceInterfaces = new ArrayList<>();

    public synchronized void collect(Class<?> serviceClass) {
        serviceInterfaces.add(serviceClass);
    }

    public List<Class<?>> getServiceInterfaces() {
        return serviceInterfaces;
    }

}
