package com.bobby.rpc.v8.server.rpcServer;

public interface IRpcServer {
    void start(int port);
    void stop();
}