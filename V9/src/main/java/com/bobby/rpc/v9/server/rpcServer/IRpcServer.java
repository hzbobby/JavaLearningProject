package com.bobby.rpc.v9.server.rpcServer;

public interface IRpcServer {
    void start(int port);
    void stop();
}