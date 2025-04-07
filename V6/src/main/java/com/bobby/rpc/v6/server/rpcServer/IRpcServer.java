package com.bobby.rpc.v6.server.rpcServer;

public interface IRpcServer {
    void start(int port);
    void stop();
}