package com.yuhuachang.NIO;

import com.yuhuachang.Request.HttpRequest;
import com.yuhuachang.WebSocket.AbstractWebSocketSession;
import com.yuhuachang.WebSocket.WebSocketSession;

import java.io.IOException;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.List;

public class SelectionKeyHandlerImp implements SelectionKeyHandler {
    private List<Channel> channels = new ArrayList<>();
    private List<HttpHandler> httpHandlers = new ArrayList<>();

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
        if (request.isWebsocketConnect()) {
            AbstractWebSocketSession handler = new WebSocketSession(socketChannel, request);
            handler.connect();
        } else if (request.isWebsocketMessage()) {
            WebSocketSession.handleDataFrame(socketChannel, request.getData());
        } else {
            for (HttpHandler handler : httpHandlers) {
                handler.handle(socketChannel, request);
            }
        }
    }
}
