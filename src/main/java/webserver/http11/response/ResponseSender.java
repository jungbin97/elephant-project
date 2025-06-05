package webserver.http11.response;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ResponseSender {

    private ResponseSender() {
        throw new IllegalStateException("Utility class");
    }

    public static void sendResponseBIO(HttpResponse response, DataOutputStream dos) throws IOException {
        byte[] body = response.getBody();
        // Content-Length
        int length = (body != null) ? body.length : 0;

        Map<String, String> headers = new HashMap<>(response.getHeaders());
        headers.putIfAbsent("Content-Length", String.valueOf(length));

        dos.writeBytes("HTTP/1.1 " + response.getStatusCode() + " " + response.getStatusMessage() + "\r\n");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            dos.writeBytes(entry.getKey() + ": " + entry.getValue() + "\r\n");
        }
        dos.writeBytes("\r\n");

        if (body != null && body.length > 0) {
            dos.write(body);
        }
        dos.flush();
    }

    public static ByteBuffer sendResponseNIO(HttpResponse response) throws IOException {
        byte[] body = response.getBody();
        int length = (body != null) ? body.length : 0;

        Map<String, String> headers = new HashMap<>(response.getHeaders());
        headers.putIfAbsent("Content-Length", String.valueOf(length));

        StringBuilder responseBuilder = new StringBuilder();

        // Status line
        responseBuilder
                .append("HTTP/1.1 ")
                .append(response.getStatusCode()).append(" ")
                .append(response.getStatusCode())
                .append("\r\n");

        // Headers
        headers.forEach((key, value) ->
                responseBuilder.append(key).append(": ").append(value).append("\r\n"));

        responseBuilder.append("\r\n");

        byte[] headerBytes = responseBuilder.toString().getBytes(StandardCharsets.ISO_8859_1);
        int totalLength = headerBytes.length + ((body != null) ? body.length : 0);
        ByteBuffer buffer = ByteBuffer.allocateDirect(totalLength);

        buffer.put(headerBytes);

        if (body != null) {
            buffer.put(body);
        }

        buffer.flip();
        return buffer;
    }

}
