package com.zeyyo.common.adb.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ClientEncoder extends MessageToByteEncoder<Object> {

    private static final String TAG = "ClientEncoder";

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object data, ByteBuf byteBuf) throws Exception {
        //自己发送过来的东西进行编码
        byteBuf.writeBytes(data.toString().getBytes());
    }
}

