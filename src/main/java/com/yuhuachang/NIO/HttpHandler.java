package com.yuhuachang.NIO;

import com.yuhuachang.Request.HttpRequest;

import java.nio.channels.SocketChannel;

public interface HttpHandler {
    void handle(SocketChannel channel, HttpRequest request);
}
