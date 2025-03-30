package com.bobby.myrpc.version8.client;

import com.bobby.myrpc.version8.common.RpcRequest;
import com.bobby.myrpc.version8.common.RpcResponse;
import com.bobby.myrpc.version8.register.IServiceRegister;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * 实现RPCClient接口
 */
public class NettyRPCClient implements IRpcClient {
    private static final Bootstrap bootstrap;
    private static final EventLoopGroup eventLoopGroup;
    // 通过注入
    private final IServiceRegister serviceRegister;

    // netty客户端初始化，重复使用
    static {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new NettyClientInitializer());
    }

    public NettyRPCClient(IServiceRegister serviceRegister) {
        this.serviceRegister = serviceRegister;
    }

    /**
     * 这里需要操作一下，因为netty的传输都是异步的，你发送request，会立刻返回， 而不是想要的相应的response
     */
    @Override
    public RpcResponse sendRequest(RpcRequest request) {
        try {
            InetSocketAddress address = serviceRegister.serviceDiscovery(request.getInterfaceName());

            ChannelFuture channelFuture = bootstrap.connect(address.getHostName(), address.getPort()).sync();
            Channel channel = channelFuture.channel();
            // 发送数据
            channel.writeAndFlush(request);
            // closeFuture: channel关闭的Future
            // sync 表示阻塞等待 它 关闭
            channel.closeFuture().sync();
            // 阻塞的获得结果，通过给channel设计别名，获取特定名字下的channel中的内容（这个在hanlder中设置）
            // AttributeKey是，线程隔离的，不会由线程安全问题。
            // 实际上不应通过阻塞，可通过回调函数
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("RPCResponse");
            RpcResponse rpcResponse = channel.attr(key).get();
            return rpcResponse;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}