package com.bobby.rpc.v7.server.rpcServer;

public interface IRpcServer {
    void start(int port);
    void stop();
}