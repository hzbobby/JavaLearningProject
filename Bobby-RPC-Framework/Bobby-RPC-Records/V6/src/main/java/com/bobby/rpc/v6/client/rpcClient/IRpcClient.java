package com.bobby.rpc.v6.client.rpcClient;


import com.bobby.rpc.v6.common.RpcRequest;
import com.bobby.rpc.v6.common.RpcResponse;

public interface IRpcClient {
    RpcResponse sendRequest(RpcRequest request);

    void close();
}