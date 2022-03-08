package com.zeyyo.common.adb.netty;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ServerEncoder extends MessageToByteEncoder<Object> {

    private static final String TAG = "ServerEncoder";

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object data, ByteBuf byteBuf) throws Exception {
        System.out.println("服务端发送到客户端进行编码"+data.getClass().getName());
        byteBuf.writeBytes(data.toString().getBytes(Charset.forName("UTF-8")));
    }
}

