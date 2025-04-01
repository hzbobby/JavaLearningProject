package com.bobby.rpc.core.server.provider;


import com.bobby.rpc.core.server.register.IServiceRegister;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ServiceProvider {
    /**
     * 一个实现类可能实现多个服务接口，
     */
    private Map<String, Object> interfaceProvider;
    private final IServiceRegister serviceRegister;
    private final InetSocketAddress socketAddress;

    public ServiceProvider(IServiceRegister serviceRegister, InetSocketAddress socketAddress) {
        this.serviceRegister = serviceRegister;
        // 需要传入服务端自身的服务的网络地址
        this.interfaceProvider = new HashMap<>();
        this.socketAddress = socketAddress;
        log.debug("服务提供者启动: {}", socketAddress.toString());
    }

    public void provideServiceInterface(Object service, boolean retryable) {
        Class<?>[] interfaces = service.getClass().getInterfaces();
        // 一个类可能实现多个服务接口
        for (Class<?> i : interfaces) {
            // 本机的映射表
            interfaceProvider.put(i.getName(), service);
            // 在注册中心注册服务
            serviceRegister.register(i.getName(), socketAddress, retryable);
        }

//        // 这里选择,是否需要使 impl 的所有接口都作为服务
//
//        interfaceProvider.put(clazz.getName(), service);
//        // 在注册中心注册服务
//        serviceRegister.register(clazz.getName(), new InetSocketAddress(host, port));
    }

    public Object getService(String interfaceName) {
        return interfaceProvider.get(interfaceName);
    }
}