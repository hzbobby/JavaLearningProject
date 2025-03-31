package com.bobby.myrpc.version6.client;

import com.bobby.myrpc.version6.RPCRequest;
import com.bobby.myrpc.version6.RPCResponse;

public interface IRPCClient {
    RPCResponse sendRequest(RPCRequest response);
}