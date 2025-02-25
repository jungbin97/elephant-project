package webserver.http11.request;

public class HttpRequestStartLine {

    private final String method;
    private final String requestUri;
    private final String httpVersion;

    public HttpRequestStartLine(String method, String requestUri, String httpVersion) {
        this.method = method;
        this.requestUri = requestUri;
        this.httpVersion = httpVersion;
    }

    public String getMethod() {
        return method;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    @Override
    public String toString() {
        return "RequestStartLine [method=" + method + ", uri=" + requestUri + ", httpVersion=" + httpVersion + "]";
    }
}
