package com.bobby.myrpc.version5.client;

import com.bobby.myrpc.version5.RPCRequest;
import com.bobby.myrpc.version5.RPCResponse;

public interface IRPCClient {
    RPCResponse sendRequest(RPCRequest response);
}