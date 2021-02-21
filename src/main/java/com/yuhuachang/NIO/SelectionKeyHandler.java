package com.yuhuachang.NIO;

import com.yuhuachang.WebSocket.WebSocketHandler;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public interface SelectionKeyHandler {
    void handle(SelectionKey key, Selector selector);
    void addHttpHandler(HttpHandler handler);
    void addWebSocketHandler(WebSocketHandler handler);
}
