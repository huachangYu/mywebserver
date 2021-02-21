package com.yuhuachang;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ChannelUtil {
    public static void writeToChannel(SocketChannel channel, String message) {
        writeToChannel(channel, message.getBytes());
    }

    public static void writeToChannel(SocketChannel channel, byte[] data) {
        if (channel == null) {
            throw new NullPointerException("channel is null");
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        try {
            channel.write(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
