package com.bobby.rpc.v6.client.proxy;

import com.bobby.rpc.v6.client.rpcClient.IRpcClient;
import com.bobby.rpc.v6.common.RpcRequest;
import com.bobby.rpc.v6.common.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ClientProxy implements InvocationHandler {
    private IRpcClient rpcClient;

    public ClientProxy(IRpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // v2.
        // 代理对象执行每个方法时，都将执行这里的逻辑
        // 我们的目的是，利用这个代理类帮助我构建请求。这样能够有效减少重复的代码
        RpcRequest request = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .paramsTypes(method.getParameterTypes())
                .params(args)
                .build();
        // 然后将这个请求发送到服务端，并获取响应
        // v6. 利用 IRpcClient 对象发送请求
        RpcResponse response = rpcClient.sendRequest(request);
        return response==null ? null : response.getData();
    }

    // 获取代理对象
    public <T> T createProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T) o;
    }
}
