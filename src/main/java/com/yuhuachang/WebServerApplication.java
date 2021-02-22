package com.yuhuachang;

import com.yuhuachang.BIO.BIOWebServer;
import com.yuhuachang.NIO.NIOWebServer;

public class WebServerApplication {
    public static void main(String[] args) {
//        new Thread(new BIOWebServer(9000)).start();
        new Thread(new NIOWebServer(9000)).start();
    }
}
