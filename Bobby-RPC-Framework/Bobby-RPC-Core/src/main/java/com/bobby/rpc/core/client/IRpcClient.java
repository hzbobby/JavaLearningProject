package com.bobby.rpc.core.client;


import com.bobby.rpc.core.common.RpcRequest;
import com.bobby.rpc.core.common.RpcResponse;

public interface IRpcClient {
    RpcResponse sendRequest(RpcRequest request);
}