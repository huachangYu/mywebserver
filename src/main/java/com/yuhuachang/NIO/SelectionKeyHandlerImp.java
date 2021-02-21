package com.yuhuachang.NIO;

import com.yuhuachang.Request.HttpRequest;
import com.yuhuachang.WebSocket.WebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.List;

public class SelectionKeyHandlerImp implements SelectionKeyHandler {
    private List<Channel> channels = new ArrayList<>();
    private List<HttpHandler> httpHandlers = new ArrayList<>();
    private List<WebSocketHandler> webSocketHandlers = new ArrayList<>();

    @Override
    public void handle(SelectionKey key, Selector selector) {
        if (key.isAcceptable()) {
            accept(key, selector);
        } else if (key.isReadable()){
            read(key, selector);
        }
    }

    @Override
    public void addHttpHandler(HttpHandler handler) {
        httpHandlers.add(handler);
    }

    @Override
    public void addWebSocketHandler(WebSocketHandler handler) {
        webSocketHandlers.add(handler);
    }

    private void registerChannel(SocketChannel channel, int opt, Selector selector) {
        try {
            channel.configureBlocking(false);
            channel.register(selector, opt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void accept(SelectionKey key, Selector selector) {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        try {
            SocketChannel channel = server.accept();
            registerChannel(channel, SelectionKey.OP_READ, selector);
            channels.add(channel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read(SelectionKey key, Selector selector) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        HttpRequest request = new HttpRequest(socketChannel);
        System.out.println(request.getMethod() + " " + request.getUrl());
        if (request.isWebsocketConnect()) {
            WebSocketHandler handler = new WebSocketHandler(socketChannel, request);
            handler.connect();
            webSocketHandlers.add(handler);
        } else if (request.isWebsocketMessage()) {

        } else {
            for (HttpHandler handler : httpHandlers) {
                handler.handle(socketChannel, request);
            }
        }
    }
}
