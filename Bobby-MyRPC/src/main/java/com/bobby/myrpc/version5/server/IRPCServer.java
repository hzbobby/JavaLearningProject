package com.bobby.myrpc.version5.server;

public interface IRPCServer {
    void start(int port);

    void stop();
}