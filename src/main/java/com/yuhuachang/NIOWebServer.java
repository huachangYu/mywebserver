package com.yuhuachang;

import com.yuhuachang.Response.HttpResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOWebServer extends AbstractWebServer implements Runnable {
    private ServerSocketChannel serverSocketChannel = null;
    private Selector selector = null;
    private int remoteClientNum = 0;

    public NIOWebServer(int port) {
        super(port);
    }

    public int getRemoteClientNum() {
        return remoteClientNum;
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
        remoteClientNum++;
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int count = 0;
        StringBuilder message = new StringBuilder();
        while ((count = socketChannel.read(byteBuffer)) > 0) {
            byteBuffer.flip();
            message.append(new String(byteBuffer.array(), "UTF-8"));
        }
        System.out.println(message);
        sendHttpResponse(socketChannel, "hello world\n");
    }

    private void sendHttpResponse(SocketChannel channel, String content) {
        new HttpResponse(channel).write(content);
    }
}
