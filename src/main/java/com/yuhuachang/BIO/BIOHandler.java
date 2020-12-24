package com.yuhuachang.BIO;

import java.io.IOException;
import java.net.Socket;

public interface BIOHandler {
    void handle(Socket clientSocket) throws IOException;
}
