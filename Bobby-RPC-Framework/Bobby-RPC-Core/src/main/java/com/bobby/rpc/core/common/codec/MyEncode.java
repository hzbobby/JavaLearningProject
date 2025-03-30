package com.bobby.rpc.core.common.codec;

import com.bobby.myrpc.version8.common.RpcRequest;
import com.bobby.myrpc.version8.common.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 依次按照自定义的消息格式写入，传入的数据为request或者response
 * 需要持有一个serialize器，负责将传入的对象序列化成字节数组
 */
@AllArgsConstructor
@Slf4j
public class MyEncode extends MessageToByteEncoder {
    private ISerializer serializer;
    
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        // 写入消息类型
        if(msg instanceof RpcRequest){
            out.writeShort(0);
        }
        else if(msg instanceof RpcResponse){
            out.writeShort(1);
        }
        // 写入序列化方式
        out.writeShort(serializer.getType());
        // 得到序列化数组
        byte[] serialize = serializer.serialize(msg);
        // 写入长度
        out.writeInt(serialize.length);
        // 写入序列化字节数组
        out.writeBytes(serialize);
    }
}