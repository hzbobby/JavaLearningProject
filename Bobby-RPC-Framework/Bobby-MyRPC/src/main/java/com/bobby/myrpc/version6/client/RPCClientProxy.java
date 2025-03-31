package com.bobby.myrpc.version6.client;

import com.bobby.myrpc.version6.RPCRequest;
import com.bobby.myrpc.version6.RPCResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RPCClientProxy implements InvocationHandler {
    private IRPCClient rpcClient;

    public RPCClientProxy(IRPCClient rpcClient) {
        this.rpcClient = rpcClient;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 代理对象执行每个方法时，都将执行这里的逻辑
        // 我们的目的是，利用这个代理类帮助我构建请求。这样能够有效减少重复的代码
        RPCRequest request = RPCRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .paramsTypes(method.getParameterTypes())
                .params(args)
                .build();
        // 然后将这个请求发送到服务端，并获取响应
        RPCResponse response = rpcClient.sendRequest(request);
        return response.getData();
    }

    // 获取代理对象
    public <T> T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T) o;
    }
}
