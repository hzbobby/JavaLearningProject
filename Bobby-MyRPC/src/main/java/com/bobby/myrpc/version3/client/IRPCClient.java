package com.bobby.myrpc.version3.client;

import com.bobby.myrpc.version3.RPCRequest;
import com.bobby.myrpc.version3.RPCResponse;

public interface IRPCClient {
    RPCResponse sendRequest(RPCRequest response);
}