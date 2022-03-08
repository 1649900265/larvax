package com.zeyyo.common.adb.netty;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class ServerDecoder extends ByteToMessageDecoder {
    private static final String TAG = "ServerDecoder";

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //收到的数据长度
        int length = byteBuf.readableBytes();
        //创建个byteBuf存储数据，进行编辑
        ByteBuf dataBuf = Unpooled.buffer(length);
        //写入收到的数据
        dataBuf.writeBytes(byteBuf);
        //将byteBuf转为数组
        String data = new String(dataBuf.array());
        System.out.println("收到了客户端发送的数据：" + data+"\n");
        //将数据传递给下一个Handler，也就是在NettyServer给ChannelPipeline添加的处理器
        list.add(data);
    }
}

