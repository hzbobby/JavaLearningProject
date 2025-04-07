package com.bobby.rpc.v8.common.codec;

import com.bobby.rpc.v8.common.codec.serializer.ISerializer;
import com.bobby.rpc.v8.common.enums.MessageType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 按照自定义的消息格式解码数据
 */
@Slf4j
@AllArgsConstructor
public class CommonDecode extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        log.debug("MyDecode$decode");

//        // version10 trace 日志
//        // 读取 traceMsg
//        int traceMsgLength = in.readInt();
//        byte[] traceBytes = new byte[traceMsgLength];
//        in.readBytes(traceBytes);
//        serializeTraceMsg(traceBytes);


        // 1. 读取消息类型
        short messageType = in.readShort();
        // 现在还只支持request与response请求
        if (messageType != MessageType.REQUEST.getCode() &&
                messageType != MessageType.RESPONSE.getCode()) {
            log.error("暂不支持此种数据: {}", messageType);
            throw new RuntimeException("暂不支持此种数据");
        }
        // 2. 读取序列化的类型
        short serializerType = in.readShort();
        // 根据类型得到相应的序列化器
        ISerializer serializer = ISerializer.getSerializerByCode(serializerType);
        if (serializer == null) throw new RuntimeException("不存在对应的序列化器");
        // 3. 读取数据序列化后的字节长度
        int length = in.readInt();
        // 4. 读取序列化数组
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        // 用对应的序列化器解码字节数组
        Object deserialize = serializer.deserialize(bytes, messageType);
        out.add(deserialize);
    }
//
//    private void serializeTraceMsg(byte[] traceByte){
//        String traceMsg=new String(traceByte);
//        String[] msgs=traceMsg.split(";");
//        if(!msgs[0].equals("")) TraceContext.setTraceId(msgs[0]);
//        if(!msgs[1].equals("")) TraceContext.setParentSpanId(msgs[1]);
//    }
}