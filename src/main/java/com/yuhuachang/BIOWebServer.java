package com.yuhuachang;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.yuhuachang.Response.HttpResponse;

public class BIOWebServer extends AbstractWebServer implements Runnable {
    protected ServerSocket serverSocket = null;
    private static int t = 0;

    public BIOWebServer(int port) {
        super(port);
    }

    public void run() {
        initServer(this.port);
        ExecutorService threadPool = Executors.newCachedThreadPool();
        while (true) {
            try {
                Socket clientSocket = this.serverSocket.accept();
                System.out.println("received msg" + t++);
                threadPool.execute(() -> {
                    try {
                        processRequestHandler(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void initServer(int port) {
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processRequestHandler(Socket clientSocket) throws IOException {
        InputStream input = clientSocket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String str;
        while ((str = reader.readLine()).equals(""))
            System.out.println(str);
        System.out.println(str);
        OutputStream output = clientSocket.getOutputStream();
        HttpResponse response = new HttpResponse(output);
        response.write("hello world\n");
        input.close();
        output.close();
        clientSocket.close();
    }
}
