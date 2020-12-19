package com.yuhuachang.NIO;

import java.nio.channels.SelectionKey;

public interface NIOHandler {
    public void read(SelectionKey key);
}
