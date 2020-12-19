package com.yuhuachang.Request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class HttpRequest {
    private InputStream input = null;
    private SocketChannel socketChannel = null;
    private List<String> headerLines = new ArrayList<>();
    String method = "";
    String url = "";

    public HttpRequest(InputStream input) {
        this.input = input;
        try {
            decodeHeader();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HttpRequest(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        try {
            decodeHeader();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    private void decodeHeader() throws IOException {
        if (input != null) {
            String line = null;
            int lineIndex = 0;
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            while (!(line = reader.readLine()).equals("")) {
                if (lineIndex == 0) {
                    String[] parts = line.split(" ");
                    method = parts[0];
                    url = parts[1];
                }
                lineIndex++;
            }
        } else if (socketChannel != null) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            int count = 0;
            StringBuilder message = new StringBuilder();
            while ((count = socketChannel.read(byteBuffer)) > 0) {
                byteBuffer.flip();
                message.append(new String(byteBuffer.array(), "UTF-8"));
            }
            String[] lines = message.toString().split("\r\n");
            String[] parts = lines[0].split(" ");
            if (parts.length >= 2) {
                method = parts[0];
                url = parts[1];
            }
        }
    }
}
