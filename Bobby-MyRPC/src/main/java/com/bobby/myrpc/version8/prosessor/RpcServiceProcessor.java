package com.bobby.myrpc.version8.prosessor;

import com.bobby.myrpc.version8.common.annotation.RpcService;
import com.bobby.myrpc.version8.register.IServiceRegister;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@RequiredArgsConstructor
@Slf4j
@Component
public class RpcServiceProcessor implements BeanPostProcessor {
    private final IServiceRegister serviceRegister;
    private final ServerProperties serverProperties;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {

        // 对所有 bean 试图获取 RpcService 注解
        RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
        if (rpcService != null) {
            register(bean, rpcService);
        }
        return bean;
    }

    private void register(Object bean, RpcService rpcService) {
        log.info("正在注册服务: {}", bean.getClass().getName());
        Class<?> interfaceClass = rpcService.interfaceClass();
        // 默认使用第一个接口
        if (interfaceClass == void.class) {
            interfaceClass = bean.getClass().getInterfaces()[0];
        }
        String serviceName = interfaceClass.getName();
        // 获取本应用的 host & port
//        serviceRegister.register(serviceName, new InetSocketAddress(serverProperties.getAddress(), 8899));
        serviceRegister.register(serviceName, new InetSocketAddress(serverProperties.getAddress(), serverProperties.getPort()));
    }

}