package com.zeyyo.common.adb.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer extends Thread{
    private static final String TAG = "NettyServer";

    //端口
    protected static final int PORT = 7010;

    @Override
    public void run() {
        super.run();
        startServer();
    }

    /**
     * 启动tcp服务端
     */
    public void startServer() {
        try {
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //添加发送数据编码器
                            pipeline.addLast(new ServerEncoder());
                            //添加解码器，对收到的数据进行解码
                            pipeline.addLast(new ServerDecoder());
                            //添加数据处理
                            pipeline.addLast(new ServerHandler());
                        }
                    });
            //服务器启动辅助类配置完成后，调用 bind 方法绑定监听端口，调用 sync 方法同步等待绑定操作完成
            b.bind(PORT).sync();
           System.out.println("TCP 服务启动成功 PORT = " + PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
