package com.yuhuachang.NIO;

import com.yuhuachang.WebSocket.AbstractWebSocketSession;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public interface SelectionKeyHandler {
    void handle(SelectionKey key, Selector selector);
    void addHttpHandler(HttpHandler handler);
}
