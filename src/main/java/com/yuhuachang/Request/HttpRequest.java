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
import java.util.Vector;

public class HttpRequest {
    private InputStream input = null;
    private SocketChannel socketChannel = null;
    private List<String> headerLines = new ArrayList<>();
    private byte[] data = null;
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

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public byte[] getData() {
        return data;
    }

    public InputStream getInput() {
        return input;
    }

    public List<String> getHeaderLines() {
        return headerLines;
    }

    private void decodeHeader() throws IOException {
        if (input != null) {
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            while (!(line = reader.readLine()).equals("")) {
                headerLines.add(line);
            }
        } else if (socketChannel != null) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            StringBuilder message = new StringBuilder();
            List<byte[]> allBytes = new Vector<>();
            while (socketChannel.read(byteBuffer) > 0) {
                byteBuffer.flip();
                allBytes.add(byteBuffer.array());
                message.append(new String(byteBuffer.array(), "UTF-8"));
            }
            if (message.length() > 0 && (message.charAt(0) > 'Z' || message.charAt(0) < 'A')) {
                int lastInd = 1023;
                int size = allBytes.size();
                for (lastInd = 1023; lastInd >= 0; lastInd--) {
                    if (allBytes.get(size - 1)[lastInd] != 0) {
                        break;
                    }
                }
                this.data = new byte[(size - 1) * 1+ lastInd + 1];
                int p = 0;
                for (byte[] bytes : allBytes) {
                    for (byte b : bytes) {
                        this.data[p++] = b;
                        if (p >= this.data.length) {
                            break;
                        }
                    }
                    if (p >= this.data.length) {
                        break;
                    }
                }
                return;
            }
            headerLines = new ArrayList<>(Arrays.asList(message.toString().split("\r\n")));
            if (headerLines.size() == 0) {
                socketChannel.close();
                return;
            }
        }

        // Get method and url
        String[] firstLineSplits = headerLines.get(0).split(" ");
        if (firstLineSplits.length >= 2) {
            method = firstLineSplits[0];
            url = firstLineSplits[1];
        } else {
            if (socketChannel != null) {
                socketChannel.close();
            }
        }
        // Decide whether the connect is a websocket
        for (String line : headerLines) {
            String[] splits = line.split(": ");
            if (splits.length >= 2 && splits[1].equals("websocket")) {
                websocket = true;
                break;
            }
        }
    }
}
