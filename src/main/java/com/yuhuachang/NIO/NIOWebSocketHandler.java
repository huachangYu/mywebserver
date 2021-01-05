package com.yuhuachang.NIO;

import java.nio.channels.SocketChannel;
import java.util.List;

public interface NIOWebSocketHandler {
    void read(SocketChannel channel, byte[] data);
}
