package com.bobby.myrpc.version1.server;

import com.bobby.myrpc.version1.RPCRequest;
import com.bobby.myrpc.version1.RPCResponse;
import com.bobby.myrpc.version1.service.IUserService;
import com.bobby.myrpc.version1.service.impl.UserServiceImpl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

public class RPCServer {

    IUserService userService = new UserServiceImpl();

    public static void main(String[] args) {
        UserServiceImpl userService = new UserServiceImpl();
        try {
            ServerSocket serverSocket = new ServerSocket(8899);
            System.out.println("服务端启动了");
            // BIO的方式监听Socket
            while (true) {
                Socket socket = serverSocket.accept();
                // 开启一个线程去处理
                new Thread(() -> {
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                        // 这里接受客户端传过来的 通用 请求
                        // 通过反射来解析
                        RPCRequest rpcRequest = (RPCRequest) ois.readObject();
                        Method method = userService.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsTypes());
                        Object result = method.invoke(userService, rpcRequest.getParams());
                        // 将结果封装到 Response
                        RPCResponse response = RPCResponse.builder().data(result).code(200).message("OK").build();
                        oos.writeObject(response);
                        oos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("从IO中读取数据错误");
                    } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                             IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("服务器启动失败");
        }
    }
}