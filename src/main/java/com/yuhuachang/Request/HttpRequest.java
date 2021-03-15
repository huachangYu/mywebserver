package com.yuhuachang.Request;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class HttpRequest {
    private InputStream input = null;
    private SocketChannel socketChannel = null;
    private List<String> headerLines = new ArrayList<>();
    private byte[] data = null;
    private boolean websocketConnect = false;
    private boolean websocketMessage = false;
    private String method = null;
    private String url = null;
    private byte[] body = null;
    private boolean isFile = false;
    private String suffix = "";

    public HttpRequest(InputStream input) {
        this.input = input;
        decodeHttpDataFrame();
    }

    public HttpRequest(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        decodeHttpDataFrame();
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

    public byte[] getBody() {
        return body;
    }

    public boolean isFile() {
        return isFile;
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

    private void decodeHttpDataFrame() {
        StringBuilder message = new StringBuilder();
        if (input != null) { // BIO模型的情况下
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
        } else if (socketChannel != null) { // NIO模型的情况下
            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                while (socketChannel.read(byteBuffer) > 0) {
                    byteBuffer.flip();
                    byte[]array = byteBuffer.array();
                    byteArrayOutputStream.write(array);
                }
                byteArrayOutputStream.flush();
                this.data = byteArrayOutputStream.toByteArray();
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (this.data.length == 0) {
            return;
        }
        message.append(new String(this.data));

        // 判断是否为websocket报文帧
        if (!message.toString().contains("HTTP/")) {
            websocketMessage = true;
            return;
        }

        List<Byte> headerLine = new ArrayList<>();
        /***
         * 0: normal, 1:'\r', 2: '\n', 3:'\r', 4: '\n'"image/jpeg
         */
        int state = 0;
        int dataInd = 0;
        for (; dataInd < this.data.length; dataInd++) {
            char c = (char) this.data[dataInd];
            if ((state == 0 && c == '\r') || (state == 1 && c == '\n') ||
                    (state == 2 && c == '\r') || (state == 3 && c == '\n')) {
                state++;
            } else {
        List<Byte> bodyBytes = new ArrayList<>();
                headerLine.add(this.data[dataInd]);
                state = 0;
            }
            if (state == 2) {
                byte[] headerLineBytes = new byte[headerLine.size()];
                for (int j = 0; j < headerLine.size(); j++) {
                    headerLineBytes[j] = headerLine.get(j);
                }
                this.headerLines.add(new String(headerLineBytes));
                headerLine.clear();
            }
            if (state == 4) break;
        }
        int bodyLength = -1;
        for (String line : this.headerLines) {
            if (line.contains("Content-Length")) {
                bodyLength = Integer.valueOf(line.split(":")[1].trim());
                break;
            }
        }

        if (bodyLength >= 0) {
            this.body = new byte[bodyLength];
            for (int i = 0; i < bodyLength; i++) {
                if (++dataInd < this.data.length) {
                    this.body[i] = this.data[dataInd];
                };
            }
        }

        // Get method and url
        String[] firstLineSplits = headerLines.get(0).split(" ");
        if (firstLineSplits.length >= 2) {
            method = firstLineSplits[0];
            url = firstLineSplits[1];
        }

        // Decide whether the connect is a websocket or a file
        for (String line : headerLines) {
            if (line.contains("image/jpeg")) {
                isFile = true;
                this.suffix = "jpg";
            } else if (line.contains("image/png")) {
                isFile = true;
                this.suffix = "png";
            }
            String[] splits = line.split(": ");
            if (splits.length >= 2 && splits[1].equals("websocket")) {
                websocketConnect = true;
                break;
            }
        }
        if (isFile) {
            try {
                UUID uid = UUID.randomUUID();
                Files.write(Path.of("/home/yuhuachang/codes/java/mywebserver/www/" + uid.toString() + "." + suffix), this.body);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
