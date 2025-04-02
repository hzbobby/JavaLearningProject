package com.bobby.rpc.core.server.netty;

import com.bobby.rpc.core.common.RpcRequest;
import com.bobby.rpc.core.common.RpcResponse;
import com.bobby.rpc.core.common.enums.RequestType;
import com.bobby.rpc.core.server.provider.ServiceProvider;
import com.bobby.rpc.core.server.ratelimit.IRateLimit;
import com.bobby.rpc.core.trace.interceptor.ServerTraceInterceptor;
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
        if(msg.getRequestType().equals(RequestType.HEARTBEAT)){
            log.info("接收到客户端的心跳包");
            return;
        }
        if(msg.getRequestType().equals(RequestType.NORMAL)){
            ServerTraceInterceptor.beforeHandle();

            RpcResponse response = getResponse(msg);

            ServerTraceInterceptor.afterHandle(msg.getMethodName());

            ctx.writeAndFlush(response);
        }
        ctx.close();
        log.debug("NettyServer 关闭连接");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        cause.printStackTrace();
        log.error("exceptionCaught: {}", cause.getMessage());
        ctx.close();
    }

    private RpcResponse getResponse(RpcRequest request) {
        // 得到服务名
        String interfaceName = request.getInterfaceName();

        // version9. 在这里做限流措施
        IRateLimit rateLimit = serviceProvider.getRateLimitProvider().getRateLimit(interfaceName);
        if(! rateLimit.getToken()){
            log.warn("服务: {} 限流!!!", interfaceName);
            return RpcResponse.fail("服务限流!!!");
        }

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