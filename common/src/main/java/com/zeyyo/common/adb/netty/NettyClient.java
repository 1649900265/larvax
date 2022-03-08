package com.zeyyo.common.adb.netty;

import android.util.Log;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


public class NettyClient {
    private static final String TAG = "NettyClient";
    private final int PORT = NettyServer.PORT;
    //连接的服务端ip地址
    private final String IP = "localhost";
    private static NettyClient nettyClient;
    //与服务端的连接通道
    private static Channel channel;
    private boolean isConnect=false;

    public static NettyClient getInstance() {
        if (nettyClient == null) {
            nettyClient = new NettyClient();
        }
        return nettyClient;
    }
    /**
     *需要在子线程中发起连接
     */
    private NettyClient() {
        new Thread(()->{
            connect();
        }).start();

    }
    /**
     * 连接服务端
     */
    private void connect() {
        try {
            NioEventLoopGroup group = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap()
                    // 指定channel类型
                    .channel(NioSocketChannel.class)
                    // 指定EventLoopGroup
                    .group(group)
                    // 指定Handler
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //添加发送数据编码器
                            pipeline.addLast(new ClientEncoder());
                            //添加数据处理器
                            pipeline.addLast(new ClientHandler());
                        }
                    });
            // 连接到服务端
            ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(IP, PORT));
            //获取连接通道
            channelFuture.sync().channel();
            channel= channelFuture.channel();
            if (channelFuture.isSuccess()){
                isConnect = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "连接失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 异步发送
     *
     * @param data 要发送的数据
     * @param listener 发送结果回调
     * @return 方法执行结果
     */
    public boolean sendMsgToServer(String data, final MessageStateListener listener) {
        boolean flag = channel != null && isConnect;
        if (flag) {
            ChannelFuture channelFuture = channel.writeAndFlush(data).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    listener.isSendSuccss(channelFuture.isSuccess());
                }
            });
        }
        return flag;
    }

}
