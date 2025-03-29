package com.bobby.myrpc.version8.server;

public interface IRPCServer {
    void start(int port);

    void stop();
}