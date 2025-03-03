package webserver.http11.request;

import java.util.Map;

public class HttpRequestHeader {
    private final Map<String, String> headers;

    public HttpRequestHeader(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    @Override
    public String toString() {
        return "RequestHeader [headers=" + headers + "]";
    }
}
