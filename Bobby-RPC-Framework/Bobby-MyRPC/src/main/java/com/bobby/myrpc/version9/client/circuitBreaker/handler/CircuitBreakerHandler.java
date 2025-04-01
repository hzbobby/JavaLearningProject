package com.bobby.myrpc.version9.client.circuitBreaker.handler;

import com.bobby.rpc.core.common.RpcRequest;
import com.bobby.rpc.core.common.RpcResponse;

public interface CircuitBreakerHandler {
    public RpcResponse handle(RpcRequest rpcRequest);
}
