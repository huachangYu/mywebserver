package com.yuhuachang.NIO;

import com.yuhuachang.AbstractWebServer;
import com.yuhuachang.Request.HttpRequest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class NIOWebServer extends AbstractWebServer implements Runnable {
    private final static ConcurrentHashMap<String, SocketChannel> websocketChannels = new ConcurrentHashMap<>();
    private ServerSocketChannel serverSocketChannel = null;
    private Selector selector = null;
    private List<NIOHandler> httpHandlers = new ArrayList<>();
    private List<NIOWebSocketHandler> webSocketHandlers = new ArrayList<>();

    public NIOWebServer(int port) {
        super(port);
    }

    @Override
    public void run() {
        initChannel(this.port);
        while (true) {
            int n = 0;
            try {
                n = selector.select(100);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (n == 0) continue;
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    try {
                        read(key);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                iterator.remove();
            }
        }
    }

    public void addHandler(NIOHandler handler) {
        httpHandlers.add(handler);
    }

    private void initChannel(int port) {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(port));
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpHandlers.add(new NIOWebServerHandler());
    }

    private void registerChannel(SocketChannel channel, int opt) {
        try {
            channel.configureBlocking(false);
            channel.register(selector, opt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void accept(SelectionKey key) {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        try {
            SocketChannel channel = server.accept();
            registerChannel(channel, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        HttpRequest request = new HttpRequest(socketChannel);
        if (request.getData() != null) {
            socketChannel.write(ByteBuffer.wrap(request.getData()));
        } else {
            System.out.println(request.getMethod() + " " + request.getUrl());
            for (NIOHandler handler : httpHandlers) {
                handler.read(socketChannel, request);
            }
        }
    }
}
