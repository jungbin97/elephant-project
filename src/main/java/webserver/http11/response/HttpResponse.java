package webserver.http11.response;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private final int statusCode;
    private final Map<String, String> headers = new HashMap<>();
    private byte[] body;

    public HttpResponse(int statusCode, String contentType, byte[] body) {
        this.statusCode = statusCode;
        this.headers.put("Content-Type", contentType);
        this.headers.put("Content-Length", String.valueOf(body.length));
        this.body = body;
    }

    public void sendResponse(DataOutputStream dos) throws IOException {
        dos.writeBytes("HTTP/1.1 " + statusCode + getStatusMessage(statusCode)+"\r\n");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            dos.writeBytes(entry.getKey() + ": " + entry.getValue() + "\r\n");
        }
        dos.writeBytes("\r\n");

        dos.write(body, 0, body.length);
        dos.flush();
    }

    private String getStatusMessage(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
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
