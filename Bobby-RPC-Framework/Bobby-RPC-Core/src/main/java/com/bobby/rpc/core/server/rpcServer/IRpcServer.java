package com.bobby.rpc.core.server.rpcServer;

public interface IRpcServer {
    void start(int port);

    void stop();
}