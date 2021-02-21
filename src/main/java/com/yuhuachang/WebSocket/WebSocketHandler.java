package com.yuhuachang.WebSocket;

import com.yuhuachang.ChannelUtil;
import com.yuhuachang.Request.HttpRequest;
import com.yuhuachang.Response.Status;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.*;

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
        send("hello");
    }

    public void read(byte[] data) {

    }

    public void send(String content) {
        if (channel != null) {
            byte[] response = encodeDataFrame("hello", 1, 1);
            ChannelUtil.writeToChannel(channel, response);
        }
    }

    public void close() {

    }

    public static int minusByte2UnsignedInt(byte b) {
        if (b >= 0) {
            throw new IllegalArgumentException("input byte must be less than 0");
        }
        return (((b ^ 0x7f) + 1) & 0xff);
    }

    public static byte[] decodeDataFrame(byte[] dataFrame) {
        int length = 0;
        if (Math.abs(dataFrame[1]) < 127) {
            length = Math.abs(dataFrame[1]);
            return Arrays.copyOfRange(dataFrame, 2, dataFrame.length);
        } else if (Math.abs(dataFrame[2]) < 127) {
//            length = (127 << 8) + (((dataFrame[2] ^ 0x7f) + 1) & 0xff);
            length = (127 << 8) + minusByte2UnsignedInt(dataFrame[2]);
            return Arrays.copyOfRange(dataFrame, 3, dataFrame.length);
        } else {
            length = ((dataFrame[6] < 0 ? minusByte2UnsignedInt(dataFrame[6]) : dataFrame[6]) << 24) +
                    ((dataFrame[7] < 0 ? minusByte2UnsignedInt(dataFrame[7]) : dataFrame[7]) << 16) +
                    ((dataFrame[8] < 0 ? minusByte2UnsignedInt(dataFrame[8]) : dataFrame[8]) << 8) +
                    (dataFrame[9] < 0 ? minusByte2UnsignedInt(dataFrame[9]) : dataFrame[9]);
            return Arrays.copyOfRange(dataFrame, 10, dataFrame.length);
        }
    }

    public static byte[] encodeDataFrame(String content, int fin, int opcode) {
        List<Byte> bytes = new Vector<>();
        bytes.add((byte) ((fin << 7) + opcode));
        int length = content.getBytes().length;
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
        for (byte b : content.getBytes()) {
            bytes.add(b);
        }
        byte[] dataFrame = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            dataFrame[i] = bytes.get(i);
        }
        return dataFrame;
    }
}
