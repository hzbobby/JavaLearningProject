package com.bobby.myrpc.version8.client;

import com.bobby.myrpc.version8.common.RpcRequest;
import com.bobby.myrpc.version8.common.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


@EnableAspectJAutoProxy(proxyTargetClass = false)
@Slf4j
public class RpcClientProxy implements InvocationHandler {
    private final IRpcClient rpcClient;

    public RpcClientProxy(IRpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.debug("RpcClientProxy 调用 invoke: " + method.getName());
        if("toString".equals(method.getName())) {
            return rpcClient.toString();
        }

        // 代理对象执行每个方法时，都将执行这里的逻辑
        // 我们的目的是，利用这个代理类帮助我构建请求。这样能够有效减少重复的代码
        RpcRequest request = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .paramsTypes(method.getParameterTypes())
                .params(args)
                .build();
        // 然后将这个请求发送到服务端，并获取响应
        RpcResponse response = rpcClient.sendRequest(request);
        return response.getData();
    }

    // 获取代理对象
    public <T> T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T) o;
    }
}
