package webserver.http11.response;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private  int statusCode;
    private final Map<String, String> headers = new HashMap<>();
    private  byte[] body;

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setBody(byte[] body) {
        this.body = body;
        headers.put("Content-Length", String.valueOf(body.length));
    }

    public void sendRedirect(String location) {
        this.statusCode = 302;
        headers.put("Location", location);
        headers.put("Content-Length", "0");
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

    public void addHeader(String key, String value) {
        headers.put(key, value);
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
