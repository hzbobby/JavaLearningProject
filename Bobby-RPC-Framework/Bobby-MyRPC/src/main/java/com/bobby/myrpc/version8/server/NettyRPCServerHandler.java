package com.bobby.myrpc.version8.server;

import com.bobby.rpc.core.common.RpcRequest;
import com.bobby.rpc.core.common.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 因为是服务器端，我们知道接受到请求格式是RPCRequest
 * Object类型也行，强制转型就行
 */
@Slf4j
@AllArgsConstructor
public class NettyRPCServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private ServiceProvider serviceProvider;
//    private IServiceRegister serviceRegister;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        log.debug("NettyServer 接收请求: {}", msg);
        RpcResponse response = getResponse(msg);
        ctx.writeAndFlush(response);
        ctx.close();
        log.debug("NettyServer 返回响应: {}", response);
        log.debug("NettyServer 关闭连接");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        System.out.println("channel close");
    }

    RpcResponse getResponse(RpcRequest request) {
        // 得到服务名
        String interfaceName = request.getInterfaceName();
        // 得到服务端相应服务实现类
        Object service = serviceProvider.getService(interfaceName);
        // 反射调用方法
        Method method = null;
        try {
            method = service.getClass().getMethod(request.getMethodName(), request.getParamsTypes());
            Object invoke = method.invoke(service, request.getParams());
            return RpcResponse.builder()
                    .code(200)
                    .data(invoke)
                    .dataType(invoke.getClass())
                    .message("OK")
                    .build();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.out.println("方法执行错误");
            return RpcResponse.fail();
        }
    }
}