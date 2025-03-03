package webserver.http11.request;

import util.HttpRequestUtils;

import java.util.Map;

public class HttpRequestStartLine {

    private final String method;
    private final String requestUri;
    private final String httpVersion;
    private final Map<String, String> queryParams;

    public HttpRequestStartLine(String method, String requestUri, String httpVersion) {
        this.method = method;
        this.httpVersion = httpVersion;
        // Parse query parameters
        int queryIndex = requestUri.indexOf("?");
        if (queryIndex != -1) {
            this.requestUri = requestUri.substring(0, queryIndex);
            String queryString = requestUri.substring(queryIndex + 1);
            this.queryParams = HttpRequestUtils.parseQueryString(queryString);
        } else {
            this.requestUri = requestUri;
            this.queryParams = Map.of();
        }
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

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    @Override
    public String toString() {
        return "RequestStartLine [method=" + method + ", uri=" + requestUri + ", httpVersion=" + httpVersion + "]";
    }
}
