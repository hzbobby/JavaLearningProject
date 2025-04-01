package com.bobby.rpc.core.client.proxy;

import com.bobby.rpc.core.client.discover.IServiceDiscover;
import com.bobby.rpc.core.client.retry.GuavaRetry;
import com.bobby.rpc.core.client.rpcClient.IRpcClient;
import com.bobby.rpc.core.common.RpcRequest;
import com.bobby.rpc.core.common.RpcResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Slf4j
public class ClientProxy implements InvocationHandler {
    private final IRpcClient rpcClient;
    private final IServiceDiscover serviceDiscover;

    public ClientProxy(IRpcClient rpcClient, IServiceDiscover serviceDiscover) {
        this.rpcClient=rpcClient;
        this.serviceDiscover=serviceDiscover;
    }

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

        // version9. 这里检查是否是可以重试的服务
        RpcResponse rpcResponse = null;
        if(serviceDiscover.retryable(request.getInterfaceName())) {
            // 这里进行重试
            rpcResponse = new GuavaRetry().sendRequestWithRetry(request, rpcClient);
        }else{
            // 不能进行重试机制
            rpcResponse = rpcClient.sendRequest(request);
        }

        log.debug("返回的消息: {}", rpcResponse);
        return rpcResponse.getData();
    }

    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> interfaceType) {
        if (!interfaceType.isInterface()) {
            throw new IllegalArgumentException(interfaceType.getName() + " is not an interface");
        }
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[] { interfaceType },
                this
        );
    }
}
