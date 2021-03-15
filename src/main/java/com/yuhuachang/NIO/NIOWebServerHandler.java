package com.yuhuachang.NIO;

import com.yuhuachang.Request.HttpRequest;
import com.yuhuachang.Response.ContentType;
import com.yuhuachang.Response.HttpResponse;
import com.yuhuachang.Response.Status;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NIOWebServerHandler implements HttpHandler {
    private static String rootPath = "www";

    @Override
    public void handle(SocketChannel socketChannel, HttpRequest request) {
        if (request.getUrl() == null) {
            // do nothing
        } else if (request.getUrl().contains("?") || request.getMethod().equals("POST")) {
            // handle get or post
            System.out.println("handle get or post");
            System.out.println(request.getUrl());
            writeMessage(socketChannel, "resp:" + request.getUrl(), ContentType.HTML);
        } else if (request.getUrl().equals("/") && request.getMethod().equals("GET")) {
            writeFile(socketChannel, "/index.html", ContentType.HTML);
        } else if (request.getMethod().equals("GET")) {
            writeFile(socketChannel, request.getUrl(), ContentType.HTML);
        }
    }

    public void writeFile(SocketChannel channel, String path, ContentType type) {
        String relativePath = rootPath + path;
        Path filePath = Paths.get(relativePath);
        try {
            byte[] bytes = Files.readAllBytes(filePath);
            new HttpResponse(channel).write(new String(bytes), type);
        } catch (IOException e) {
            new HttpResponse(channel).write("not found", type, Status.NOT_FOUND);
            e.printStackTrace();
        }
    }

    public void writeMessage(SocketChannel channel, String message, ContentType type) {
        new HttpResponse(channel).write(message, type);
    }
}
