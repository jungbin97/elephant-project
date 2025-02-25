package webserver.http11.request;

public class HttpRequest {
    private final HttpRequestStartLine startLine;
    private final HttpRequestHeader headers;
    private final HttpRequestBody body;

    public HttpRequest(HttpRequestStartLine startLine, HttpRequestHeader headers, HttpRequestBody body) {
        this.startLine = startLine;
        this.headers = headers;
        this.body = body;
    }

    public HttpRequestStartLine getStartLine() {
        return startLine;
    }

    public HttpRequestHeader getHeaders() {
        return headers;
    }

    public HttpRequestBody getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "HttpRequest [startLine=" + startLine + ", header=" + headers + ", body=" + body + "]";
    }
}
