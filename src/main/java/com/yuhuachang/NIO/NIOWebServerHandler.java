package com.yuhuachang.NIO;

import com.yuhuachang.Request.HttpRequest;
import com.yuhuachang.Response.ContentType;
import com.yuhuachang.Response.HttpResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class NIOWebServerHandler implements NIOHandler{
    private static String rootPath = "www/";

    @Override
    public void read(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        HttpRequest request = new HttpRequest(socketChannel);
        System.out.println(request.getMethod() + " " + request.getUrl());
        if (request.getUrl().equals("/")) {
            writeFile(socketChannel, "index.html");
        }
    }

    public void writeFile(SocketChannel channel, String path) {
        String relativePath = rootPath + path;
        StringBuilder sb = new StringBuilder();
        File file = new File(relativePath);
        int fileLength = (int) file.length();
        byte[] bytes = new byte[fileLength];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytes);
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new HttpResponse(channel).write(new String(bytes), ContentType.HTML);
    }
}
