package com.bobby.rpc.core.server;

public interface IRPCServer {
    void start(int port);

    void stop();
}