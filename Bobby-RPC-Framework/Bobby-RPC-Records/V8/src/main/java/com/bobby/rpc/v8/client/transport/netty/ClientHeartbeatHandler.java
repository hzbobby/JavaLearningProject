package com.bobby.rpc.v8.client.transport.netty;

import com.bobby.rpc.v8.common.RpcRequest;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientHeartbeatHandler extends ChannelDuplexHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent idleStateEvent) {

            IdleState idleState = idleStateEvent.state();

            if(idleState == IdleState.WRITER_IDLE) {
                RpcRequest heartBeat = RpcRequest.heartBeat();
                ctx.writeAndFlush(heartBeat);
                log.info("超过8秒没有写数据，发送心跳包: {}", heartBeat);
            }

        }else {
            super.userEventTriggered(ctx, evt);
        }
    }
}