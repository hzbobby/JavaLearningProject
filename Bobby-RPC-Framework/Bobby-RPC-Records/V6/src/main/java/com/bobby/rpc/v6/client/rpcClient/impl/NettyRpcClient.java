package com.bobby.rpc.v6.client.rpcClient.impl;

import com.bobby.rpc.v6.client.discover.IServiceDiscover;
import com.bobby.rpc.v6.client.rpcClient.IRpcClient;
import com.bobby.rpc.v6.client.transport.netty.NettyClientInitializer;
import com.bobby.rpc.v6.common.RpcRequest;
import com.bobby.rpc.v6.common.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 实现RPCClient接口
 */
@Slf4j
public class NettyRpcClient implements IRpcClient {
    private static final Bootstrap bootstrap;
    private static final EventLoopGroup eventLoopGroup;
    private static final NettyClientInitializer nettyClientInitializer;

    // 通过注入
    private final IServiceDiscover serviceDiscover;

    // netty客户端初始化，重复使用
    static {
        nettyClientInitializer = new NettyClientInitializer();
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(nettyClientInitializer);
    }

    public NettyRpcClient(IServiceDiscover serviceDiscover) {
        this.serviceDiscover = serviceDiscover;
    }

    /**
     * 这里需要操作一下，因为netty的传输都是异步的，你发送request，会立刻返回， 而不是想要的相应的response
     */
    @Override
    public RpcResponse sendRequest(RpcRequest request) {
        InetSocketAddress address = serviceDiscover.serviceDiscovery(request.getInterfaceName());
        log.debug("RPC$远程服务地址: {}", address);
        if (address == null) {
            log.error("服务发现失败，返回的地址为 null");
            return RpcResponse.fail("服务发现失败，地址为 null");
        }
        try {
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
        }
        return null;
    }

    public void close() {
        // 关闭 netty
        if(eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully().addListener(future -> {
                if (future.isSuccess()) {
                    log.info("关闭 Netty 成功");
                }else{
                    log.info("关闭 Netty 失败");
                }
            });
            try {
                eventLoopGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                log.error("关闭 Netty 异常: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

}