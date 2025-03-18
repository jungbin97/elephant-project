package webserver.http11.request;


public class HttpRequestStartLine {

    private final String method;
    private final String requestUri;
    private final String httpVersion;

    public HttpRequestStartLine(String method, String requestUri, String httpVersion) {
        this.method = method;
        this.httpVersion = httpVersion;
        this.requestUri = requestUri;
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
        return "HttpRequestStartLine [method=" + method + ", requestUri=" + requestUri + ", httpVersion=" + httpVersion + "]";
    }
}
