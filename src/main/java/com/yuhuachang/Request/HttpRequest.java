package com.yuhuachang.Request;

import com.yuhuachang.WebSocket.AbstractWebSocketSession;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HttpRequest {
    private InputStream input = null;
    private SocketChannel socketChannel = null;
    private List<String> headerLines = new ArrayList<>();
    private byte[] data = null;
    private boolean websocketConnect = false;
    private boolean websocketMessage = false;
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

    public boolean isWebsocketConnect() {
        return websocketConnect;
    }

    public boolean isWebsocketMessage() {
        return websocketMessage;
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
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while (socketChannel.read(byteBuffer) > 0) {
                byteBuffer.flip();
                byte[] array = byteBuffer.array();
                byteArrayOutputStream.write(array);
                message.append(new String(array, "UTF-8"));
            }
            byteArrayOutputStream.flush();
            this.data = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();

            // 判断是否为websocket报文帧
            if (!message.toString().contains("HTTP/")) {
                System.out.println(Arrays.toString(this.data));
                websocketMessage = true;
                return;
            }
            System.out.println(message);
            headerLines = new ArrayList<>(Arrays.asList(message.toString().split("\r\n")));
        }

        // Get method and url
        String[] firstLineSplits = headerLines.get(0).split(" ");
        if (firstLineSplits.length >= 2) {
            method = firstLineSplits[0];
            url = firstLineSplits[1];
        }
        // Decide whether the connect is a websocket
        for (String line : headerLines) {
            String[] splits = line.split(": ");
            if (splits.length >= 2 && splits[1].equals("websocket")) {
                websocketConnect = true;
                break;
            }
        }
    }
}
