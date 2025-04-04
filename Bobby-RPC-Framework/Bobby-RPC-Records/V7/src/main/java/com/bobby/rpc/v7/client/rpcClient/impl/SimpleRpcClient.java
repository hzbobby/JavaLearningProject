package com.bobby.rpc.v7.client.rpcClient.impl;

import com.bobby.rpc.v7.client.rpcClient.IRpcClient;
import com.bobby.rpc.v7.common.RpcRequest;
import com.bobby.rpc.v7.common.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SimpleRpcClient implements IRpcClient {

    private String host;
    private int port;
    public SimpleRpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public RpcResponse sendRequest(RpcRequest request) {
        try {
            Socket socket = new Socket(host, port);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            // 发送请求
            objectOutputStream.writeObject(request);
            // 获取响应
            RpcResponse response = (RpcResponse) objectInputStream.readObject();
            return response;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("SimpleRpcClient, sendRequest Exception: "+e.getMessage());
            return null;
        }
    }

    @Override
    public void close() {

    }
}