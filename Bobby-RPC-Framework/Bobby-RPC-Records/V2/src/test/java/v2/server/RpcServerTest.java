package v2.server;

import com.bobby.rpc.v2.common.RpcRequest;
import com.bobby.rpc.v2.common.RpcResponse;
import com.bobby.rpc.v2.sample.service.impl.UserServiceImpl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

public class RpcServerTest {
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
                        RpcRequest rpcRequest = (RpcRequest) ois.readObject();
                        Method method = userService.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsTypes());
                        Object result = method.invoke(userService, rpcRequest.getParams());
                        // 将结果封装到 Response
                        RpcResponse response = RpcResponse.builder().data(result).code(200).message("OK").build();
                        oos.writeObject(response);
                        oos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("从IO中读取数据错误");
                    } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
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
