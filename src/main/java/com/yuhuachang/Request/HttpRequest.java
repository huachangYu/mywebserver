package com.yuhuachang.Request;

import java.io.*;
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
            System.out.println(message);
            System.out.println(this.data);
            headerLines = new ArrayList<>(Arrays.asList(message.toString().split("\r\n")));
        }

        // Decide whether the message is a websocket dataframe
        if (headerLines.size() == 0) {
            websocketMessage = true;
            return;
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
