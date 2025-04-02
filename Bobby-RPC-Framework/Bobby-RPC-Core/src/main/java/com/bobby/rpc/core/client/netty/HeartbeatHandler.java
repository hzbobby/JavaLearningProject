package com.bobby.rpc.core.client.netty;

import com.bobby.rpc.core.common.RpcRequest;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartbeatHandler extends ChannelDuplexHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;

            IdleState idleState = idleStateEvent.state();

            if(idleState == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush(RpcRequest.heartBeat());
                log.info("超过8秒没有写数据，发送心跳包");
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }
}