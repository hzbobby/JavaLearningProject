package com.bobby.myrpc.version8.client;


import com.bobby.myrpc.version8.common.RpcRequest;
import com.bobby.myrpc.version8.common.RpcResponse;

public interface IRpcClient {
    RpcResponse sendRequest(RpcRequest request);
}