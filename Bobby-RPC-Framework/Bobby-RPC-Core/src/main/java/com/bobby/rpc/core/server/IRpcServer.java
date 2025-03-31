package com.bobby.rpc.core.server;

public interface IRpcServer {
    void start(int port);

    void stop();
}