package com.yuhuachang.WebSocket;

import com.yuhuachang.Request.HttpRequest;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class WebSocketSession extends AbstractWebSocketSession {
    private static List<WebSocketSession> webSocketSessions = new ArrayList<>();

    public WebSocketSession(SocketChannel channel, HttpRequest connectionRequest) {
        super(channel, connectionRequest);
    }

    private final static int OPCODE_STRING = 0x1;
    private final static int OPCODE_CLOSE = 0x8;

    public static void handleDataFrame(SocketChannel channel, byte[] dataFrame) {
        if (dataFrame.length == 0) {
            return;
        }
        int opcode = dataFrame[0] & 15;
        switch (opcode) {
            case OPCODE_STRING:
                onMessage(decodeDataFrame(dataFrame));
                break;
            case OPCODE_CLOSE:
                for (WebSocketSession session : webSocketSessions) {
                    if (session.getChannel() == channel) {
                        session.close();
                        break;
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void connect() {
        super.connect();
        webSocketSessions.add(this);
    }

    @Override
    public void close() {
        super.close();
        webSocketSessions.remove(this);
    }

    public static void onMessage(byte[] data) {
        String content = new String(data);
        System.out.println(content);
        for (WebSocketSession session : webSocketSessions) {
            session.send(content);
        }
    }

}
