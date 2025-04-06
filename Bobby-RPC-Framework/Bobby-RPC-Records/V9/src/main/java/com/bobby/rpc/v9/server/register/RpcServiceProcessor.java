package com.bobby.rpc.v9.server.register;

import com.bobby.rpc.v9.common.annotation.RpcService;
import com.bobby.rpc.v9.server.provider.ServiceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;

@Slf4j
public class RpcServiceProcessor implements BeanPostProcessor {
    private final ServiceProvider serviceProvider;

    public RpcServiceProcessor(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

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
//        Class<?> interfaceClass = rpcService.interfaceClass();
//        // 默认使用第一个接口
//        if (interfaceClass == void.class) {
//            interfaceClass = bean.getClass().getInterfaces()[0];
//        }
//        String serviceName = interfaceClass.getName();
//         获取本应用的 host & port
        serviceProvider.provideServiceInterface(bean, rpcService.retryable());
    }

}