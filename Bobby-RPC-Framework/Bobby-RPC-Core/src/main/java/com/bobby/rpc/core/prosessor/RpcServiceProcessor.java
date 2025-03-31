package com.bobby.rpc.core.prosessor;

import com.bobby.rpc.core.common.annotation.RpcService;
import com.bobby.rpc.core.server.ServiceProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;

@RequiredArgsConstructor
@Slf4j
public class RpcServiceProcessor implements BeanPostProcessor {
    //    private final IServiceRegister serviceRegister;
    private final ServiceProvider serviceProvider;

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
        log.info("RpcServiceProcessor$register 正在注册服务: {}", bean.getClass().getName());
        Class<?> interfaceClass = rpcService.interfaceClass();
        // 默认使用第一个接口
        if (interfaceClass == void.class) {
            interfaceClass = bean.getClass().getInterfaces()[0];
        }
//        String serviceName = interfaceClass.getName();
        // 获取本应用的 host & port
//        serviceRegister.register(serviceName, new InetSocketAddress(serverProperties.getAddress(), nettyProperties.getPort()));
//        serviceRegister.register(serviceName, new InetSocketAddress(serverProperties.getAddress(), serverProperties.getPort()));
        serviceProvider.provideServiceInterface(bean, interfaceClass);
    }

}