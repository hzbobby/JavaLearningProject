package com.bobby.myrpc.version9.client.rpcClient;


import com.bobby.rpc.core.common.RpcRequest;
import com.bobby.rpc.core.common.RpcResponse;

public interface IRpcClient {
    RpcResponse sendRequest(RpcRequest request);
}