package com.bobby.myrpc.version4.server;

public interface IRPCServer {
    void start(int port);

    void stop();
}