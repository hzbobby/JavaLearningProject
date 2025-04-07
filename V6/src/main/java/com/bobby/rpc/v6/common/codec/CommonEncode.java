package com.bobby.rpc.v6.common.codec;

import com.bobby.rpc.v6.common.RpcRequest;
import com.bobby.rpc.v6.common.RpcResponse;
import com.bobby.rpc.v6.common.codec.serializer.ISerializer;
import com.bobby.rpc.v6.common.enums.MessageType;
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
public class CommonEncode extends MessageToByteEncoder {
    private ISerializer serializer;
    
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        log.debug("MyEncode$encode mgs of Type: {}", msg.getClass());

        /**
         * 协议格式：
         * +----------------+---------------------+------------------+------------------+
         * |  消息类型        |   序列化方式          |  序列化长度        |  序列化字节       |
         * |  (2 Byte)      |   (4 Byte)          |  (4 Byte)        |  (变长)          |
         * +----------------+---------------------+------------------+------------------+
        **/

//        // version10. 写入 trace 消息头
//        String traceMsg = String.format("%s;%s", TraceContext.getTraceId(), TraceContext.getSpanId());
//        byte[] traceBytes = traceMsg.getBytes();
//
//        // 写入 traceMsg 长度
//        out.writeInt(traceBytes.length);
//        // 写入 traceMsg
//        out.writeBytes(traceBytes);


        // 写入消息类型
        if(msg instanceof RpcRequest){
            out.writeShort(MessageType.REQUEST.getCode());
        }
        else if(msg instanceof RpcResponse){
            out.writeShort(MessageType.RESPONSE.getCode());
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