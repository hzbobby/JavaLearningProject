package com.bobby.myrpc.version7.client;

import com.bobby.myrpc.version7.RPCRequest;
import com.bobby.myrpc.version7.RPCResponse;

public interface IRPCClient {
    RPCResponse sendRequest(RPCRequest response);
}