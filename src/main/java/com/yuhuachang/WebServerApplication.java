package com.yuhuachang;

public class WebServerApplication {
    public static void main(String[] args) {
//        new Thread(new BIOWebServer(9000)).start();
        new Thread(new NIOWebServer(9000)).start();
    }
}
