package com.bobby.rpc.v4.server.rpcServer;

public interface IRpcServer {
    void start(int port);
    void stop();
}