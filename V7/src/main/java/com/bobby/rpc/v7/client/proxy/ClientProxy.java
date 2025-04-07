package com.bobby.rpc.v7.client.proxy;

import com.bobby.rpc.v7.client.circuitBreaker.CircuitBreaker;
import com.bobby.rpc.v7.client.circuitBreaker.CircuitBreakerProvider;
import com.bobby.rpc.v7.client.rpcClient.IRpcClient;
import com.bobby.rpc.v7.common.RpcRequest;
import com.bobby.rpc.v7.common.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Slf4j
public class ClientProxy implements InvocationHandler {
    private IRpcClient rpcClient;
    private final CircuitBreakerProvider circuitBreakerProvider;


    public ClientProxy(IRpcClient rpcClient, CircuitBreakerProvider circuitBreakerProvider) {
        this.rpcClient = rpcClient;
        this.circuitBreakerProvider = circuitBreakerProvider;
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
                .type(RpcRequest.RequestType.NORMAL)
                .build();

        // v7 熔断器
        //获取熔断器
        CircuitBreaker circuitBreaker= circuitBreakerProvider.getCircuitBreaker(method.getName());
        //判断熔断器是否允许请求经过
        if (!circuitBreaker.allowRequest()){
            //这里可以针对熔断做特殊处理，返回特殊值
            log.info("服务被熔断了");
            return RpcResponse.fail("服务被熔断了");
        }

        // 然后将这个请求发送到服务端，并获取响应
        // v6. 利用 IRpcClient 对象发送请求
        RpcResponse response = rpcClient.sendRequest(request);

        // v7 根据响应信息，更新熔断器状态

        if (response != null) {
            if (response.getCode() == 200) {
                circuitBreaker.recordSuccess();
            } else if (response.getCode() == 500) {
                circuitBreaker.recordFailure();
            }
            log.info("收到响应: {} 状态码: {}", request.getInterfaceName(), response.getCode());
            return response.getData();
        }

        return null;
    }

    // 获取代理对象
    public <T> T createProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T) o;
    }
}
