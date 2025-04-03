package com.bobby.rpc.v6.server.transport.netty;

import com.bobby.rpc.v6.common.codec.CommonDecode;
import com.bobby.rpc.v6.common.codec.CommonEncode;
import com.bobby.rpc.v6.common.codec.serializer.ISerializer;
import com.bobby.rpc.v6.server.provider.ServiceProvider;
import com.bobby.rpc.v6.server.transport.netty.NettyRpcServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * 初始化，主要负责序列化的编码解码， 需要解决netty的粘包问题
 */
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    private final ServiceProvider serviceProvider;

    public NettyServerInitializer(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

//        // 消息格式 [长度][消息体], 解决粘包问题
//        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
//        // 计算当前待发送消息的长度，写入到前4个字节中
//        pipeline.addLast(new LengthFieldPrepender(4));
//
//        // 这里使用的还是java 序列化方式， netty的自带的解码编码支持传输这种结构
//        pipeline.addLast(new ObjectEncoder());
//        pipeline.addLast(new ObjectDecoder(new ClassResolver() {
//            @Override
//            public Class<?> resolve(String className) throws ClassNotFoundException {
//                return Class.forName(className);
//            }
//        }));

        // 使用自定义的编解码器
        pipeline.addLast(new CommonDecode());
        // 编码需要传入序列化器，这里是json，还支持ObjectSerializer，也可以自己实现其他的
        pipeline.addLast(new CommonEncode(ISerializer.getSerializerByCode(ISerializer.SerializerType.JSON.getCode())));
//        // 通过 SPI 机制 + 配置项 来指定使用的 序列化方式
//        // 我们就可以通过配置项来动态修改 ISerializer 实现类。而且，新增序列化机制，也不需要修改 ISerializer
//        pipeline.addLast(new CommonEncode(SerializerSpiLoader.getInstance(serializer)));

        pipeline.addLast(new NettyRpcServerHandler(serviceProvider));
    }
}