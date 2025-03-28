package com.bobby.myrpc.version4.client;

import com.bobby.myrpc.version4.RPCRequest;
import com.bobby.myrpc.version4.RPCResponse;

public interface IRPCClient {
    RPCResponse sendRequest(RPCRequest response);
}