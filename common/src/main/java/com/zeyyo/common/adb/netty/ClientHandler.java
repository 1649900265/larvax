package com.zeyyo.common.adb.netty;

import android.util.Log;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientHandler extends SimpleChannelInboundHandler<Object> {

    private static final String TAG = "ClientHandler";

    /**
     * 当收到数据的回调
     *
     * @param channelHandlerContext 封装的连接对像
     * @param o
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        ByteBuf byteBuf= ((ByteBuf)o);
//        int length = byteBuf.readableBytes();
//        String logcat= byteBuf.toString(Charset.forName("UTF-8"));
//        CharSequence charSequence=  byteBuf.readCharSequence(length,Charset.forName("UTF-8"));
        String charSequence=new String(byteBuf.array(),Charset.forName("UTF-8"));
        System.out.println("channelRead0logcat:"+charSequence.length());
    }

    /**
     * 与服务端连接成功的回调
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Log.d(TAG, "与服务端连接成功" + ctx.toString());
    }

    /**
     * 与服务端断开的回调
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Log.d(TAG, "与服务端断开连接：" + ctx.toString());
    }
}

