package com.bobby.myrpc.version8.server;

import com.bobby.myrpc.version8.register.IServiceRegister;
import com.bobby.myrpc.version8.register.ZkServiceRegister;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

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
        this.serviceRegister = serviceRegister;
        // 需要传入服务端自身的服务的网络地址
        this.host = host;
        this.port = port;
        this.interfaceProvider = new HashMap<>();
    }

    public void provideServiceInterface(Object service) {
        Class<?>[] interfaces = service.getClass().getInterfaces();

        for (Class clazz : interfaces) {
            // 本机的映射表
            interfaceProvider.put(clazz.getName(), service);
            // 在注册中心注册服务
            serviceRegister.register(clazz.getName(), new InetSocketAddress(host, port));
        }

    }

    public Object getService(String interfaceName) {
        return interfaceProvider.get(interfaceName);
    }
}