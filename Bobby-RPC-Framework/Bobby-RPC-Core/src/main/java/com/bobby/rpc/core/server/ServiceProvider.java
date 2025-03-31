package com.bobby.rpc.core.server;


import com.bobby.rpc.core.register.IServiceRegister;
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
    private String host;
    private int port;

    public ServiceProvider(IServiceRegister serviceRegister) {
        this.serviceRegister = serviceRegister;
    }

    public ServiceProvider(IServiceRegister serviceRegister, String host, int port) {
        log.info("服务提供者启动 {}:{}", host, port);
        this.serviceRegister = serviceRegister;
        // 需要传入服务端自身的服务的网络地址
        this.host = host;
        this.port = port;
        this.interfaceProvider = new HashMap<>();
    }

    public void provideServiceInterface(Object service, Class<?> clazz) {
        Class<?>[] interfaces = service.getClass().getInterfaces();
//        // 一个类可能实现多个服务接口
//        for (Class<?> i : interfaces) {
//            // 本机的映射表
//            interfaceProvider.put(i.getName(), service);
//            // 在注册中心注册服务
//            serviceRegister.register(i.getName(), new InetSocketAddress(host, port));
//        }

        // 这里选择,是否需要使 impl 的所有接口都作为服务

        interfaceProvider.put(clazz.getName(), service);
        // 在注册中心注册服务
        serviceRegister.register(clazz.getName(), new InetSocketAddress(host, port));
    }

    public Object getService(String interfaceName) {
        return interfaceProvider.get(interfaceName);
    }
}