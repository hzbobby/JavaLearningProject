package com.bobby.rpc.v3.server.rpcServer;

public interface IRpcServer {
    void start(int port);
    void stop();
}