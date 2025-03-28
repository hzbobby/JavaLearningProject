package com.bobby.myrpc.version1.client;

import com.bobby.myrpc.version1.RPCRequest;
import com.bobby.myrpc.version1.RPCResponse;
import com.bobby.myrpc.version1.domain.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

public class RPCClient {
    public static void main(String[] args) {
        try {
            // 建立Socket连接
            Socket socket = new Socket("127.0.0.1", 8899);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            // 客户端构造请求
            RPCRequest request = RPCRequest.builder()
                    .interfaceName("com.bobby.rpc.service.IUserService")
                    .methodName("getUserById")
                    .paramsTypes(new Class[]{Long.class})
                    .params(new Object[]{new Random().nextLong()})
                    .build();
            // 发送请求
            objectOutputStream.writeObject(request);
            RPCResponse response = (RPCResponse) objectInputStream.readObject();

            System.out.println("服务端返回的User:" + response.toString());

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("客户端启动失败");
        }
    }
}