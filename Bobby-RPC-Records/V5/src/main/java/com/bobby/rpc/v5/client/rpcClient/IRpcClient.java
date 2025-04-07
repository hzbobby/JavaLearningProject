package com.bobby.rpc.v5.client.rpcClient;


import com.bobby.rpc.v5.common.RpcRequest;
import com.bobby.rpc.v5.common.RpcResponse;

public interface IRpcClient {
    RpcResponse sendRequest(RpcRequest request);
}