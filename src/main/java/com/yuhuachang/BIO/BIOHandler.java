package com.yuhuachang.BIO;

import com.yuhuachang.Request.HttpRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public interface BIOHandler {
    void handle(HttpRequest request, OutputStream output);
}
