package com.yuhuachang.WebSocket;

import com.yuhuachang.ChannelUtil;
import com.yuhuachang.Request.HttpRequest;
import com.yuhuachang.Response.Status;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WebSocketHandler {
    private static String CRLF = "\r\n";
    private static String VERSION = "HTTP/1.1";
    private static String MAGIC_STRING = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private HttpRequest request = null;
    private SocketChannel channel = null;
    private String url = null;
    private List<String> headerLines = new ArrayList<>();

    public WebSocketHandler(SocketChannel channel,  HttpRequest request) {
        this.request = request;
        this.url = request.getUrl();
        this.channel = channel;
    }

    private void fillHeaderLines() {
        headerLines.clear();
        String key = "";
        List<String> requestHeaderLines = request.getHeaderLines();
        for (String line : requestHeaderLines) {
            String[] splits = line.split(": ");
            if (splits.length >= 2 && splits[0].equals("Sec-WebSocket-Key")) {
                String secWebsocketKey = splits[1];
                byte[] sha1 = DigestUtils.sha1((secWebsocketKey + MAGIC_STRING).getBytes());
                key = new String(Base64.encodeBase64(sha1));
            }
        }
        headerLines.add(VERSION + " " + Status.SWITCH_PROTOCOLS.toString());
        headerLines.add("Connection: Upgrade");
        SimpleDateFormat greenwichDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        String date = greenwichDate.format(Calendar.getInstance().getTime());
        headerLines.add("Date: " + date);
        headerLines.add("Upgrade: websocket");
        headerLines.add("Sec-WebSocket-Accept: " + key);
    }

    public void connect() {
        this.fillHeaderLines();
        if (channel != null) {
            for (String line : headerLines) {
                ChannelUtil.writeToChannel(channel, line + CRLF);
            }
            ChannelUtil.writeToChannel(channel, CRLF);
        }
    }

    public void read(byte[] data) {

    }

    public void send(String content) {

    }

    public void close() {

    }

    private byte[] encode(String content) {
        return null;
    }
}
