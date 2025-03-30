package com.bobby.myrpc.version8.prosessor;

import com.bobby.myrpc.version8.client.RpcClientProxy;
import com.bobby.myrpc.version8.common.annotation.RpcReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/30
 */

@Slf4j
public class RpcReferenceProcessor implements BeanPostProcessor {
    // 在这里需要注入网络通信 Client
    // 这里直接注入代理客户端
    private final RpcClientProxy rpcClientProxy;

    public RpcReferenceProcessor(RpcClientProxy rpcClientProxy) {
        this.rpcClientProxy = rpcClientProxy;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                log.info("正在注入服务: {}", field.getName());

                // 实现类似 DubboReference
                // 接口是公共模块的
                // 接口的实现不在同一台服务器上
                // 我们通过代理类，为接口的每个调用构造请求
                // 通过远程调用来获取结果
                Class<?> rpcReferenceInterface = rpcReference.interfaceClass();
                if (rpcReferenceInterface == void.class) {
                    rpcReferenceInterface = field.getDeclaringClass().getInterfaces()[0];
                }
                // 根据接口获取代理类对象
                // 生成代理对象并注入
                Object proxy = rpcClientProxy.getProxy(rpcReferenceInterface);
                field.setAccessible(true);
                try {
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("注入RPC服务失败", e);
                }
                log.info("{} 服务注入到 bean {}", field.getName(), beanName);
            }
        }
        return bean;
    }

}
