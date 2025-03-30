package com.bobby.myrpc.version8.config;

import com.bobby.myrpc.version8.biz.service.IBlogService;
import com.bobby.myrpc.version8.biz.service.IRpcDemoService;
import com.bobby.myrpc.version8.biz.service.IUserService;
import com.bobby.myrpc.version8.client.IRpcClient;
import com.bobby.myrpc.version8.client.NettyRPCClient;
import com.bobby.myrpc.version8.client.RpcClientProxy;
import com.bobby.myrpc.version8.common.RpcRequest;
import com.bobby.myrpc.version8.common.RpcResponse;
import com.bobby.myrpc.version8.common.annotation.RpcReference;
import com.bobby.myrpc.version8.factory.ProxyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@RequiredArgsConstructor
@Slf4j
@Component
public class ServiceRegisterListener implements ApplicationListener<ContextRefreshedEvent> {
    private final RpcClientProxy rpcClientProxy;
    private final IRpcClient rpcClient;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("容器时间监听器");
        // 容器完全启动后执行注册
        IRpcDemoService bean = event.getApplicationContext().getBean(IRpcDemoService.class);
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field f : declaredFields) {
            RpcReference annotation = f.getAnnotation(RpcReference.class);
            if (annotation != null) {
                discoverService(bean, f, rpcClientProxy, annotation);
            }
        }
    }

    public void discoverService(Object bean, Field field, RpcClientProxy rpcClientProxy, RpcReference rpcReference) {

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
        IUserService proxy = ProxyFactory.createProxy(IUserService.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if("toString".equals(method.getName())) {
                    return proxy.toString();
                }

                log.debug("自定义代理");
                RpcRequest request = RpcRequest.builder()
                        .interfaceName(method.getDeclaringClass().getName())
                        .methodName(method.getName())
                        .paramsTypes(method.getParameterTypes())
                        .params(args)
                        .build();
                log.debug("发送的请求: {}", request);
                RpcResponse rpcResponse = rpcClient.sendRequest(request);
                log.debug("返回的消息: {}", rpcResponse);
                return rpcResponse.getData();
            }
        });
        field.setAccessible(true);
        try {
            field.set(bean, proxy);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("注入RPC服务失败", e);
        }
        log.info("{} 服务注入到 bean {} 的字段 {}", field.getName(), bean.getClass().getName(), field.getName());
    }

}