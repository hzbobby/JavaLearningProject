package com.bobby.myrpc.version9.client.proxy;

import com.bobby.rpc.core.client.rpcClient.IRpcClient;
import com.bobby.rpc.core.common.RpcRequest;
import com.bobby.rpc.core.common.RpcResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@Slf4j
@RequiredArgsConstructor
public class InvokeHandler implements InvocationHandler {
    private final IRpcClient rpcClient;
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("toString".equals(method.getName())) {
            return proxy.toString();
        }
        log.debug("走代理类");
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
}
