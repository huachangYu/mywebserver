package com.yuhuachang;

public abstract class AbstractWebServer {
    protected int port = 8080;

    public AbstractWebServer(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }
}
