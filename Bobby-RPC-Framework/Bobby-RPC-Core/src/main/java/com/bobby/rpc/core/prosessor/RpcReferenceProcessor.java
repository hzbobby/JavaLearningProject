package com.bobby.rpc.core.prosessor;

import com.bobby.rpc.core.common.annotation.RpcReference;
import com.bobby.rpc.core.factory.ProxyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/30
 */

@RequiredArgsConstructor
@Slf4j
public class RpcReferenceProcessor implements BeanPostProcessor {
    private final InvocationHandler rpcClientInvocationHandler;


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                log.debug("找到一个 RpcReference 的字段 {}", field.getName());
                // 实现类似 DubboReference
                // 接口是公共模块的
                // 接口的实现不在同一台服务器上
                // 我们通过代理类，为接口的每个调用构造请求
                // 通过远程调用来获取结果
                Class<?> rpcReferenceInterface = rpcReference.interfaceClass();
                if (rpcReferenceInterface == void.class) {
                    rpcReferenceInterface = field.getType();
                }
                // 根据接口获取代理类对象
                // 生成代理对象并注入
                log.debug("rpcReferenceInterface: {}", rpcReferenceInterface);

                Object proxy = ProxyFactory.createProxy(rpcReferenceInterface, rpcClientInvocationHandler);
                field.setAccessible(true);
                try {
                    log.debug("代理类注入 bean: {}, declareField: {}, proxy: {}", bean.getClass().getTypeName(), field.getName(), proxy.getClass().getTypeName());
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("注入RPC服务失败", e);
                }
            }
        }
        return bean;
    }

}
