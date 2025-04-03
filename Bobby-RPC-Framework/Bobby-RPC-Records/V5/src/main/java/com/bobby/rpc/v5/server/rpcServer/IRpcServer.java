package com.bobby.rpc.v5.server.rpcServer;

public interface IRpcServer {
    void start(int port);
    void stop();
}