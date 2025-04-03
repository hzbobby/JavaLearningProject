package com.bobby.rpc.core.client.proxy;

import com.bobby.rpc.core.client.circuitBreaker.CircuitBreaker;
import com.bobby.rpc.core.client.circuitBreaker.CircuitBreakerProvider;
import com.bobby.rpc.core.client.discover.IServiceDiscover;
import com.bobby.rpc.core.client.retry.GuavaRetry;
import com.bobby.rpc.core.client.rpcClient.IRpcClient;
import com.bobby.rpc.core.trace.interceptor.ClientTraceInterceptor;
import com.bobby.rpc.core.common.RpcRequest;
import com.bobby.rpc.core.common.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Slf4j
public class ClientProxy implements InvocationHandler {
    private final IRpcClient rpcClient;
    private final IServiceDiscover serviceDiscover;
    private final CircuitBreakerProvider circuitBreakerProvider;

    public ClientProxy(IRpcClient rpcClient, IServiceDiscover serviceDiscover,CircuitBreakerProvider circuitBreakerProvider) {
        this.rpcClient = rpcClient;
        this.serviceDiscover = serviceDiscover;
        this.circuitBreakerProvider = circuitBreakerProvider;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // version10 分布式日志
        ClientTraceInterceptor.beforeInvoke();  // 先记录一些信息

        log.debug("走代理类");
        RpcRequest request = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .paramsTypes(method.getParameterTypes())
                .params(args)
                .build();
        log.debug("发送的请求: {}", request);

        // version9 熔断器
        //获取熔断器
        CircuitBreaker circuitBreaker= circuitBreakerProvider.getCircuitBreaker(method.getName());
        //判断熔断器是否允许请求经过
        if (!circuitBreaker.allowRequest()){
            //这里可以针对熔断做特殊处理，返回特殊值
            return RpcResponse.fail("服务被熔断了");
        }

        // version9. 这里检查是否是可以重试的服务
        RpcResponse response = null;
        if(serviceDiscover.retryable(request.getInterfaceName())) {
            // 这里进行重试
            try{
                response = new GuavaRetry().sendRequestWithRetry(request, rpcClient);
            }catch(Exception e){
                // version9 熔断器
                circuitBreaker.recordFailure();
                throw e;
            }
        }else{
            // 不能进行重试机制
            response = rpcClient.sendRequest(request);
        }
        // version9 根据响应信息，更新熔断器状态

        if (response != null) {
            if (response.getCode() == 200) {
                circuitBreaker.recordSuccess();
            } else if (response.getCode() == 500) {
                circuitBreaker.recordFailure();
            }
            log.info("收到响应: {} 状态码: {}", request.getInterfaceName(), response.getCode());
        }

        log.debug("返回的消息: {}", response);

        // version10 请求结束，上报日志
        ClientTraceInterceptor.afterInvoke(method.getName());

        return response==null ? null : response.getData();
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

    // 方法签名

    // 根据接口名字和方法获取方法签名
    private String getMethodSignature(String interfaceName, Method method) {
        // 拼接一个方法
        // 接口名#方法(方法参数)#返回值
        StringBuilder sb = new StringBuilder();
        sb.append(interfaceName).append("#").append(method.getName()).append("(");
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            sb.append(parameterTypes[i].getName());
            if (i < parameterTypes.length - 1) {
                sb.append(",");
            } else{
                sb.append(")");
            }
        }
        sb.append("#");
        sb.append(method.getReturnType().getName());
        return sb.toString();
    }
}
