package com.zeyyo.common.adb.netty;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


public class ServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final String TAG = "ServerHandler";

    /**
     * 当收到数据的回调
     *
     * @param channelHandlerContext 封装的连接对像
     * @param o
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o)  {
       System.out.println("收到了解码器处理过的数据：" + o.toString());
       try {
//           String[] cmd=new String[]{"logcat -b main"};
           Process process = Runtime.getRuntime().exec("sh");
           DataOutputStream os = new DataOutputStream(process.getOutputStream());
           os.writeBytes("logcat -G 16m\n");
           os.writeBytes("logcat\n");
           os.flush();
        // NOTE: You can write to stdin of the command using
        // process.getOutputStream().
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        System.out.println("执行logcat");
        int read;
        char[] buffer = new char[1024];
        StringBuffer output = new StringBuffer();
        while ((read = reader.read(buffer)) > 0) {
            output.append(buffer, 0, read);
//            System.out.println("输出logcat日志"+output.length());
            if(output.length()>1024*4) {
                System.out.println("给客户端发送日志"+output.length());
//                ByteBuf byteBuf = Unpooled.copiedBuffer(output.toString().getBytes(CharsetUtil.UTF_8));
                channelHandlerContext.channel().writeAndFlush(output);
                output.setLength(0);
            }
        }
        reader.close();
        // Waits for the command to finish.
        process.waitFor();

       } catch (IOException e) {
           channelHandlerContext.channel().writeAndFlush("error:"+e.getMessage());
           e.printStackTrace();
       } catch (InterruptedException e) {
           channelHandlerContext.channel().writeAndFlush("error:"+e.getMessage());
           e.printStackTrace();
       }catch (Exception e){
           channelHandlerContext.channel().writeAndFlush("error:"+e.getMessage());
       }
    }

    /**
     * 有客户端连接过来的回调
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        System.out.println("有客户端连接过来：" + ctx.toString());
    }

    /**
     * 有客户端断开了连接的回调
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("有客户端断开了连接：" + ctx.toString());
    }
}
