package com.yuhuachang.WebSocket;

import com.yuhuachang.ChannelUtil;
import com.yuhuachang.Request.HttpRequest;
import com.yuhuachang.Response.Status;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class AbstractWebSocketSession {
    private static String CRLF = "\r\n";
    private static String VERSION = "HTTP/1.1";
    private static String MAGIC_STRING = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private HttpRequest connectionRequest = null;
    private SocketChannel channel = null;
    private String url = null;
    private List<String> headerLines = new ArrayList<>();

    public AbstractWebSocketSession(SocketChannel channel, HttpRequest connectionRequest) {
        this.connectionRequest = connectionRequest;
        this.url = connectionRequest.getUrl();
        this.channel = channel;
    }

    private void fillHeaderLines() {
        headerLines.clear();
        String key = "";
        List<String> requestHeaderLines = connectionRequest.getHeaderLines();
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

    public void send(String content) {
        if (channel != null) {
            byte[] response = encodeDataFrame(content, 1, 1);
            ChannelUtil.writeToChannel(channel, response);
        }
    }

    public void close() {
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] decodeDataFrame(byte[] dataFrame) {
        int i = 0;
        int fin = (dataFrame[i] & 0xff) >>> 7;
        int opcode = dataFrame[i++] & 15;
        int mask = (dataFrame[i] & 0xff) >>> 7;
        int payloadLength = dataFrame[i++] & 0x7f;
        if (payloadLength == 126) {
            payloadLength = (dataFrame[i++] << 8) + dataFrame[i++];
        } else if (payloadLength == 127) {
            payloadLength = (dataFrame[i++] << 24) + (dataFrame[i++] << 16) +
                    (dataFrame[i++] << 8) + dataFrame[i++];
        }
        byte[] data = new byte[payloadLength];
        if (mask == 1) {
            byte[] maskingKey = new byte[] {dataFrame[i++], dataFrame[i++], dataFrame[i++], dataFrame[i++]};
            for (int j = 0; j < payloadLength; j++) {
                data[j] = (byte) (dataFrame[i+j] ^ maskingKey[j % 4]);
            }
        } else {
            data = Arrays.copyOfRange(dataFrame, i, i + payloadLength);
        }
        return data;
    }

    public static byte[] encodeDataFrame(String content, int fin, int opcode) {
        return encodeDataFrame(content.getBytes(), fin, opcode);
    }

    public static byte[] encodeDataFrame(byte[] data, int fin, int opcode) {
        List<Byte> bytes = new Vector<>();
        bytes.add((byte) ((fin << 7) + opcode));
        int length = data.length;
        if (length < 126) {
            bytes.add((byte) length);
        } else if (length < 0x10000) {
            bytes.addAll(Arrays.asList(
                    (byte) 126,
                    (byte) (length & 0xFF00 >> 8),
                    (byte) (length & 0xFF))
            );
        } else {
            bytes.addAll(Arrays.asList(
                    (byte) 127, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
                    (byte) ((length & 0xFF000000) >> 24),
                    (byte) ((length & 0xFF0000) >> 16),
                    (byte) ((length & 0xFF00) >> 8),
                    (byte) (length & 0xFF)));
        }
        for (byte b : data) {
            bytes.add(b);
        }
        byte[] dataFrame = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            dataFrame[i] = bytes.get(i);
        }
        return dataFrame;
    }

    public SocketChannel getChannel() {
        return channel;
    }
}
