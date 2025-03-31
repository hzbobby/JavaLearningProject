package com.bobby.rpc.core.config;

import com.bobby.rpc.core.common.annotation.RpcReference;
import com.bobby.rpc.core.factory.ProxyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Component
public class ServiceScanListener implements ApplicationListener<ContextRefreshedEvent> {
    private final InvocationHandler rpcClientInvocationHandler;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.debug("RpcReference 发现");
        // 容器完全启动后执行注册
        Map<String, Object> serviceBeans = event.getApplicationContext().getBeansWithAnnotation(Service.class);
        for (Map.Entry<String, Object> entry : serviceBeans.entrySet()) {
            String serviceName = entry.getKey();
            Object bean = entry.getValue();
            Field[] declaredFields = bean.getClass().getDeclaredFields();
            for (Field declaredField : declaredFields) {
                RpcReference annotation = declaredField.getAnnotation(RpcReference.class);
                if (annotation != null) {
                    log.debug("找到一个 RpcReference 的字段 {}", declaredField.getName());
                    // 为这个字段注入代理类
                    Class<?> referenceClass = annotation.interfaceClass();
                    if (referenceClass == void.class) {
                        referenceClass = declaredField.getType();
                    }
                    log.debug("referenceClass: {}", referenceClass);
                    Object proxy = ProxyFactory.createProxy(referenceClass, rpcClientInvocationHandler);
                    declaredField.setAccessible(true);
                    try {
                        log.debug("bean: {}, declareField: {}, proxy: {}", bean.getClass().getTypeName(), declaredField.getName(), proxy.getClass().getTypeName());
                        declaredField.set(bean, proxy);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

    }

}