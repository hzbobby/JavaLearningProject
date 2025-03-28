package com.bobby.myrpc.version3.server;

public interface IRPCServer {
    void start(int port);

    void stop();
}