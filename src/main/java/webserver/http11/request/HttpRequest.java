package webserver.http11.request;

import java.util.Map;

public class HttpRequest {
    private final HttpRequestStartLine startLine;
    private final HttpRequestHeader headers;
    private final HttpRequestBody body;
    private final Map<String, String> queryParameters;

    public HttpRequest(HttpRequestStartLine startLine, HttpRequestHeader headers, HttpRequestBody body, Map<String, String> queryParameters) {
        this.startLine = startLine;
        this.headers = headers;
        this.body = body;
        this.queryParameters = queryParameters;
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

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public HttpCookie getCookies() {
        return new HttpCookie(getHeaders().getHeaders().get("Cookie"));
    }

    @Override
    public String toString() {
        return "HttpRequest [startLine=" + startLine + ", header=" + headers + ", body=" + body + "]";
    }
}
