package com.bobby.myrpc.version2.server;

public interface IRPCServer {
    void start(int port);

    void stop();
}