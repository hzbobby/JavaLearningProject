package com.bobby.rpc.v4.client.rpcClient;


import com.bobby.rpc.v4.common.RpcRequest;
import com.bobby.rpc.v4.common.RpcResponse;

public interface IRpcClient {
    RpcResponse sendRequest(RpcRequest request);
}