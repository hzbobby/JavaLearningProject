package com.bobby.rpc.v7.server.transport.netty;

import com.bobby.rpc.v7.common.RpcRequest;
import com.bobby.rpc.v7.common.RpcResponse;
import com.bobby.rpc.v7.server.provider.ServiceProvider;
import com.bobby.rpc.v7.server.ratelimit.IRateLimit;
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
        if (request.getType().equals(RpcRequest.RequestType.HEARTBEAT)) {
            log.info("接收到客户端的心跳包");
//            ctx.flush();
            return;
        }
        if (request.getType().equals(RpcRequest.RequestType.NORMAL)) {
            RpcResponse response = getResponse(request);
            log.info("返回响应: {}", response);
            ctx.writeAndFlush(response);
        }
        ctx.close();
        log.info("NettyServer 关闭连接");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("通信异常: {}", cause.getMessage());
        ctx.close();
    }

    private RpcResponse getResponse(RpcRequest request) {
        // 得到服务名
        String interfaceName = request.getInterfaceName();

        // ve7. 在这里做限流措施
        IRateLimit rateLimit = serviceProvider.getRateLimitProvider().getRateLimit(interfaceName);
        if (!rateLimit.getToken()) {
            log.info("服务: {} 限流!!!", interfaceName);
            return RpcResponse.fail("服务限流!!!");
        }

        // 得到服务端相应服务实现类
        Object service = serviceProvider.getService(interfaceName);
        // 反射调用方法
        Method method = null;
        try {
            method = service.getClass().getMethod(request.getMethodName(), request.getParamsTypes());
            Object ret = method.invoke(service, request.getParams());
            // 某些操作可能没有返回值
            Class<?> dataType = null;
            if (ret != null) {
                dataType = ret.getClass();
            }
            return RpcResponse.builder()
                    .code(200)
                    .data(ret)
                    .dataType(dataType)
                    .message("OK")
                    .build();
        } catch (NoSuchMethodException | IllegalAccessException | NullPointerException | InvocationTargetException e) {
            e.printStackTrace();
            return RpcResponse.fail();
        }
    }
}