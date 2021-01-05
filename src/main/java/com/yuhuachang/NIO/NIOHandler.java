package com.yuhuachang.NIO;

import com.yuhuachang.Request.HttpRequest;

import java.nio.channels.SocketChannel;

public interface NIOHandler {
    void read(SocketChannel channel, HttpRequest request);
}
