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
        } else if (request.getUrl().equals("/")) {
            writeFile(socketChannel, "/index.html", ContentType.HTML);
        } else {
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
            new HttpResponse(channel).write("", type, Status.NOT_FOUND);
            e.printStackTrace();
        }
    }
}
