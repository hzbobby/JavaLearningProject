package com.bobby.rpc.core.server.rpcServer.impl;

import com.bobby.rpc.core.server.netty.NettyServerInitializer;
import com.bobby.rpc.core.server.provider.ServiceProvider;
import com.bobby.rpc.core.server.rpcServer.IRpcServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 实现RPCServer接口，负责监听与发送数据
 */
@Slf4j
public class NettyRpcServer implements IRpcServer {
    private final ServiceProvider serviceProvider;
    private NettyServerInitializer nettyServerInitializer;
    private ChannelFuture channelFuture;

    public NettyRpcServer(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void start(int port) {
        // netty 服务线程组boss负责建立连接， work负责具体的请求
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            // 启动netty服务器
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            nettyServerInitializer = new NettyServerInitializer(serviceProvider);
            // 初始化
            serverBootstrap
                    .group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(nettyServerInitializer);
            // 同步阻塞
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            // 死循环监听
            channelFuture.channel().closeFuture().sync();


        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            shutdown(bossGroup, workGroup);
        }
        //        channelFuture = serverBootstrap.bind(port);
        //        channelFuture.addListener((ChannelFuture future) -> {
        //            if (future.isSuccess()) {
        //                log.info("Netty 服务启动 port {}", port);
        //            } else {
        //                log.error("Netty 服务启动失败 port {}", port);
        //            }
        //        });
    }

    @Override
    public void stop() {
        if (channelFuture != null) {
            try {
                channelFuture.channel().close().sync();
                log.info("Netty服务端主通道已关闭");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("关闭Netty服务端主通道时中断：{}", e.getMessage(), e);
            }
        } else {
            log.warn("Netty服务端主通道尚未启动，无法关闭");
        }
    }

    private void shutdown(NioEventLoopGroup bossGroup, NioEventLoopGroup workGroup) {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().syncUninterruptibly();
        }
        if (workGroup != null) {
            workGroup.shutdownGracefully().syncUninterruptibly();
        }
    }

    public void setSerializer(String serializer) {
        nettyServerInitializer.setSerializer(serializer);
    }
}