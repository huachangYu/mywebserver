package com.yuhuachang.Request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HttpRequest {
    private InputStream input = null;
    private SocketChannel socketChannel = null;
    private List<String> headerLines = new ArrayList<>();
    private boolean websocket = false;
    private String method = null;
    private String url = null;

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

    public boolean isWebsocket() {
        return websocket;
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
                headerLines.add(line);
            }
        } else if (socketChannel != null) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            StringBuilder message = new StringBuilder();
            while (socketChannel.read(byteBuffer) > 0) {
                byteBuffer.flip();
                message.append(new String(byteBuffer.array(), "UTF-8"));
            }
            headerLines = new ArrayList<>(Arrays.asList(message.toString().split("\r\n")));
        }
        if (headerLines.size() == 0) {
            return;
        }

        // Get method and url
        String[] firstLineSplits = headerLines.get(0).split(" ");
        if (firstLineSplits.length >= 2) {
            method = firstLineSplits[0];
            url = firstLineSplits[1];
        } else {
            socketChannel.close();
        }
        // decide whether the connect is a websocket
        for (String line : headerLines) {
            String splits[] = line.split(": ");
            if (splits.length >=2 && splits[1].equals("websocket")) {
                websocket = true;
            }
        }
    }
}
