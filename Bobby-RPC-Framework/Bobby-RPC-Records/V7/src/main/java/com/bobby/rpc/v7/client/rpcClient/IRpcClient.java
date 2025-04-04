package com.bobby.rpc.v7.client.rpcClient;


import com.bobby.rpc.v7.common.RpcRequest;
import com.bobby.rpc.v7.common.RpcResponse;

public interface IRpcClient {
    RpcResponse sendRequest(RpcRequest request);

    void close();
}