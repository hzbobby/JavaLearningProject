package com.bobby.rpc.v9.client.rpcClient;


import com.bobby.rpc.v9.common.RpcRequest;
import com.bobby.rpc.v9.common.RpcResponse;

public interface IRpcClient {
    RpcResponse sendRequest(RpcRequest request);

    void close();
}