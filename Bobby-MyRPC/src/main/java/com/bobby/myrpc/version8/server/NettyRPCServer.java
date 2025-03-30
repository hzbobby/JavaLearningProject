package com.bobby.myrpc.version8.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 实现RPCServer接口，负责监听与发送数据
 */
@Slf4j
@AllArgsConstructor
public class NettyRPCServer implements IRPCServer {
    private final ServiceProvider serviceProvider;

    @Override
    public void start(int port) {
        // netty 服务线程组boss负责建立连接， work负责具体的请求
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            // 启动netty服务器
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            // 初始化
            serverBootstrap
                    .group(bossGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyServerInitializer(serviceProvider));
            // 同步阻塞
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
//            // 死循环监听
//            channelFuture.channel().closeFuture().sync();

            serverBootstrap.bind(port).addListener((ChannelFuture future) -> {
                if(future.isSuccess()) {
                    log.info("Netty Server started on port {}", port);
                }else{
                    log.error("Netty Server failed to start on port {}", port);
                }
            });

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
//            bossGroup.shutdownGracefully();
//            workGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() {
    }
}