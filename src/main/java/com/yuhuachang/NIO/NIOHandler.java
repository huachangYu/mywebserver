package com.yuhuachang.NIO;

import java.nio.channels.SelectionKey;

public interface NIOHandler {
    void read(SelectionKey key);
}
