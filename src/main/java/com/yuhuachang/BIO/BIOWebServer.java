package com.yuhuachang.BIO;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.yuhuachang.AbstractWebServer;
import com.yuhuachang.Request.HttpRequest;
import com.yuhuachang.Response.ContentType;
import com.yuhuachang.Response.HttpResponse;

public class BIOWebServer extends AbstractWebServer implements Runnable {
    private ServerSocket serverSocket = null;
    private List<BIOHandler> handlers = new ArrayList<>();

    public BIOWebServer(int port) {
        super(port);
    }

    public void run() {
        initServer(this.port);
        ExecutorService threadPool = Executors.newCachedThreadPool();
        while (true) {
            try {
                Socket clientSocket = this.serverSocket.accept();
                threadPool.execute(() -> {
                    try {
                        for (BIOHandler handler : handlers) {
                            handler.handle(clientSocket);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addHandler(BIOHandler handler) {
        handlers.add(handler);
    }

    @Override
    protected void initServer(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        handlers.add(clientSocket -> {
            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();
            HttpRequest request = new HttpRequest(input);
            System.out.println(request.getMethod() + " " + request.getUrl());
            HttpResponse response = new HttpResponse(output);
            response.write("hello world\n", ContentType.HTML);
            input.close();
            output.close();
            clientSocket.close();
        });
    }
}
