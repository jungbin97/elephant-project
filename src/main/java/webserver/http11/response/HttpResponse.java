package webserver.http11.response;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private final int statusCode;
    private final Map<String, String> headers = new HashMap<>();
    private final byte[] body;

    public HttpResponse(int statusCode, String contentType, byte[] body) {
        this.statusCode = statusCode;
        this.body = body != null ? body : new byte[0];
        if (contentType != null && !contentType.isEmpty()) {
            headers.put("Content-Type", contentType);
        }
        this.headers.put("Content-Length", String.valueOf(this.body.length));
    }

    public HttpResponse(int statusCode, String location) {
        if (statusCode != 301 && statusCode != 302 && statusCode != 307 && statusCode != 308) {
            throw new IllegalArgumentException("Invalid status code for redirect: " + statusCode);
        }
        this.statusCode = statusCode;
        this.body = new byte[0];
        this.headers.put("Location", location);
        this.headers.put("Content-Length", "0");
    }

    public void sendResponse(DataOutputStream dos) throws IOException {
        dos.writeBytes("HTTP/1.1 " + statusCode + getStatusMessage(statusCode)+"\r\n");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            dos.writeBytes(entry.getKey() + ": " + entry.getValue() + "\r\n");
        }
        dos.writeBytes("\r\n");

        if (body != null && body.length > 0) {
            dos.write(body, 0, body.length);
        }
        dos.flush();
    }

    private String getStatusMessage(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 307 -> "Temporary Redirect";
            case 308 -> "Permanent Redirect";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Unknown Status";
        };
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public byte[] getBody() {
        return body;
    }
}
