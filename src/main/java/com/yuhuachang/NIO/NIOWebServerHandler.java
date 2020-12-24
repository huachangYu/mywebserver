package com.yuhuachang.NIO;

import com.yuhuachang.Request.HttpRequest;
import com.yuhuachang.Response.ContentType;
import com.yuhuachang.Response.HttpResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NIOWebServerHandler implements NIOHandler{
    private static String rootPath = "www";

    @Override
    public void read(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        HttpRequest request = new HttpRequest(socketChannel);
        System.out.println(request.getMethod() + " " + request.getUrl());
        if (request.getUrl().equals("/")) {
            writeFile(socketChannel, "/index.html");
        } else {
            writeFile(socketChannel, request.getUrl());
        }
    }

    public void writeFile(SocketChannel channel, String path) {
        String relativePath = rootPath + path;
        Path filePath = Paths.get(relativePath);
        try {
            byte[] bytes = Files.readAllBytes(filePath);
            new HttpResponse(channel).write(new String(bytes), ContentType.HTML);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
