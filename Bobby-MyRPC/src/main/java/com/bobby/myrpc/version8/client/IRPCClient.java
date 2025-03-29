package com.bobby.myrpc.version8.client;

import com.bobby.myrpc.version8.common.RPCRequest;
import com.bobby.myrpc.version8.common.RPCResponse;

public interface IRPCClient {
    RPCResponse sendRequest(RPCRequest response);
}