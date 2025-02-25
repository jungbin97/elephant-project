package webserver.http11.request;

import java.util.Map;

public class HttpRequestHeader {
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String CHUNKED_LENGTH = "Chunked-Length";
    private final Map<String, String> headers;

    public HttpRequestHeader(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public int getContentLength() {
        return Integer.parseInt(headers.get(CONTENT_LENGTH));
    }

    public boolean isContentLength() {
        return headers.containsKey(CONTENT_LENGTH);
    }

    public int getChunkedLength() {
        return Integer.parseInt(headers.get(CHUNKED_LENGTH));
    }

    public boolean isChunked() {
        return headers.containsKey(CHUNKED_LENGTH);
    }

    @Override
    public String toString() {
        return "RequestHeader [headers=" + headers + "]";
    }
}
