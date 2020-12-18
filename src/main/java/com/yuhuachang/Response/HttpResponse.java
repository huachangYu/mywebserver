package com.yuhuachang.Response;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HttpResponse {
    private static String CRLF = "\r\n";
    private String version = "HTTP/1.1";
    private OutputStream output = null;
    private SocketChannel channel = null;
    private List<String> headerLines = new ArrayList<>();

    public HttpResponse(OutputStream output) {
        this.output = output;
    }

    public HttpResponse(SocketChannel channel) {
        this.channel = channel;
    }

    private void fillHeader(Status status, ContentType type) {
        headerLines.clear();
        headerLines.add(version + " " + status.toString());
        headerLines.add("Access-Control-Allow-Origin: *");
        headerLines.add("Connection: Keep-Alive");
        headerLines.add(type.toString() + ";charset=UTF-8");
        SimpleDateFormat greenwichDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        String date = greenwichDate.format(Calendar.getInstance().getTime());
        headerLines.add("Date: " + date);
    }

    public void write(String content) {
        fillHeader(Status.OK, ContentType.TXT);
        headerLines.add("Content-Length: " + content.length());
        if (output != null) {
            try (DataOutputStream dataOutputStream = new DataOutputStream(output)){
                for (String line : headerLines) {
                    dataOutputStream.writeBytes(line + CRLF);
                }
                dataOutputStream.writeBytes(CRLF);
                dataOutputStream.writeBytes(content);
                dataOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (channel != null) {
            for (String line : headerLines) {
                writeToChannel(line + CRLF);
            }
            writeToChannel(CRLF);
            writeToChannel(content);
        }
    }

    private void writeToChannel(String str) {
        if (channel == null) {
            throw new NullPointerException("channel is null");
        }
        byte[] bytes = str.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        try {
            channel.write(byteBuffer);
        } catch (IOException e) {
            try {
                channel.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            e.printStackTrace();
        }
    }
}
