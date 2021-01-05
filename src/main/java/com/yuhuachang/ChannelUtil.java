package com.yuhuachang;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ChannelUtil {
    public static void writeToChannel(SocketChannel channel, String message) {
        if (channel == null) {
            throw new NullPointerException("channel is null");
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(message.getBytes());
        try {
            channel.write(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
