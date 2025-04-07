package com.bobby.rpc.v8.client.rpcClient;


import com.bobby.rpc.v8.common.RpcRequest;
import com.bobby.rpc.v8.common.RpcResponse;

public interface IRpcClient {
    RpcResponse sendRequest(RpcRequest request);

    void close();
}