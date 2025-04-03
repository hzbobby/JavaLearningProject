package com.bobby.rpc.v6.server.transport.netty;

import com.bobby.rpc.v6.common.RpcRequest;
import com.bobby.rpc.v6.common.RpcResponse;
import com.bobby.rpc.v6.server.provider.ServiceProvider;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 因为是服务器端，我们知道接受到请求格式是RPCRequest
 * Object类型也行，强制转型就行
 */
@Slf4j
public class NettyRpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private final ServiceProvider serviceProvider;
    public NettyRpcServerHandler(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        log.info("NettyServer 接收请求: {}", request);
        RpcResponse response = getResponse(request);
        ctx.writeAndFlush(response);
//        ctx.close();
//        log.info("NettyServer 关闭连接");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exceptionCaught: {}", cause.getMessage());
        ctx.close();
    }

    private RpcResponse getResponse(RpcRequest request) {
        // 得到服务名
        String interfaceName = request.getInterfaceName();

        // 得到服务端相应服务实现类
        Object service = serviceProvider.getService(interfaceName);
        // 反射调用方法
        Method method = null;
        try {
            method = service.getClass().getMethod(request.getMethodName(), request.getParamsTypes());
            Object ret = method.invoke(service, request.getParams());
            // 某些操作可能没有返回值
            Class<?> dataType = null;
            if (ret != null){
                dataType = ret.getClass();
            }
            return RpcResponse.builder()
                    .code(200)
                    .data(ret)
                    .dataType(dataType)
                    .message("OK")
                    .build();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return RpcResponse.fail();
        }
    }
}