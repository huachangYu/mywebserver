package com.yuhuachang.NIO;

import com.yuhuachang.AbstractWebServer;
import com.yuhuachang.Request.HttpRequest;
import com.yuhuachang.WebSocket.WebSocketHandler;

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
    private SelectionKeyHandlerImp selectionKeyHandlerImp = new SelectionKeyHandlerImp();


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
                selectionKeyHandlerImp.handle(key, selector);
                iterator.remove();
            }
        }
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
        selectionKeyHandlerImp.addHttpHandler(new NIOWebServerHandler());
    }
}
