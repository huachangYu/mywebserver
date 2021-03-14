package com.yuhuachang.Request;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private String body = null;
    private boolean isFile = false;
    private String suffix = "";

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

    public String getBody() {
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

    private void decodeHeader() {
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
        if (this.data.length == 0) {
            return;
        }
        message.append(new String(this.data));

        // 判断是否为websocket报文帧
        if (!message.toString().contains("HTTP/")) {
            websocketMessage = true;
            return;
        }

        String[] headerBody = message.toString().split("\r\n\r\n");
        String header = headerBody[0];
        if (headerBody[0].contains("image/png")) {
            this.isFile = true;
            this.suffix = ".png";
        }
        String body = headerBody[1];

        String[] lines = header.split("\r\n");
        for (String line : lines) {
            headerLines.add(line);
        }

        // Get method and url
        String[] firstLineSplits = headerLines.get(0).split(" ");
        if (firstLineSplits.length >= 2) {
            method = firstLineSplits[0];
            url = firstLineSplits[1];
        }
        if (method.equals("POST")) {
            if (headerBody.length >= 3) {
                for (int i = 1; i < headerBody.length - 1; i++) {
                    if (headerBody[i].contains("image/png")) {
                        this.isFile = true;
                        this.suffix = "png";
                    }
                    String[] nextLines = headerBody[i].split("\r\n");
                    for (String line : nextLines) {
                        headerLines.add(line);
                    }
                }
                body = headerBody[headerBody.length - 1];
            }
        }

        this.body = body;

        // Decide whether the connect is a websocket
        for (String line : headerLines) {
            String[] splits = line.split(": ");
            if (splits.length >= 2 && splits[1].equals("websocket")) {
                websocketConnect = true;
                break;
            }
        }
        if (isFile) {
            try {
                byte[] bytes = this.body.getBytes(StandardCharsets.UTF_8);
                int capacity = bytes.length;
                for (String line : headerLines) {
                    if (line.contains("Content-Length")) {
                        capacity = Integer.valueOf(lines[9].split(":")[1].trim());
                        break;
                    }
                }
                byte[] fileData = Arrays.copyOf(bytes, capacity);
                Files.write(Path.of("/home/yuhuachang/codes/java/mywebserver/www/tmp" + suffix), fileData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("headerlines:");
        for (String line : headerLines) {
            System.out.println(line);
        }
        System.out.println("body:");
        System.out.println(this.body);
        System.out.println(body.trim().length());
    }
}
