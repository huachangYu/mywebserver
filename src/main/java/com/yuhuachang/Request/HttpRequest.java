package com.yuhuachang.Request;

import com.yuhuachang.WebSocket.AbstractWebSocketSession;

import java.io.*;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

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
        decodeHeader();
    }

    public HttpRequest(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        decodeHeader();
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

    private void decodeHeader() {
        StringBuilder message = new StringBuilder();
        if (input != null) {
            byte b;
            List<Byte> bytes = new Vector<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            try {
                while ((b = (byte) reader.read()) != -1) {
                    bytes.add(b);
                }
            } catch (IOException ignored) {

            }
            this.data = new byte[bytes.size()];
            for (int i = 0; i < bytes.size(); i++) {
                this.data[i] = bytes.get(i);
            }
        } else if (socketChannel != null) {
            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                while (socketChannel.read(byteBuffer) > 0) {
                    byteBuffer.flip();
                    byte[] array = byteBuffer.array();
                    byteArrayOutputStream.write(array);
//                message.append(new String(array, StandardCharsets.UTF_8));
                }
                byteArrayOutputStream.flush();
                this.data = byteArrayOutputStream.toByteArray();
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        message.append(new String(this.data));
        // 判断是否为websocket报文帧
        if (!message.toString().contains("HTTP/")) {
            System.out.println(Arrays.toString(this.data));
            websocketMessage = true;
            return;
        }
        System.out.println(message);
        headerLines = new ArrayList<>(Arrays.asList(message.toString().split("\r\n")));

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
