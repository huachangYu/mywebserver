package com.yuhuachang.WebSocket;

import com.yuhuachang.AbstractWebServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebSocketServer extends AbstractWebServer implements Runnable {
    private ServerSocket serverSocket = null;
    public WebSocketServer(int port) {
        super(port);
    }

    @Override
    public void run() {
        this.initServer(this.port);
        ExecutorService threadPool = Executors.newCachedThreadPool();
    }

    @Override
    protected void initServer(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
