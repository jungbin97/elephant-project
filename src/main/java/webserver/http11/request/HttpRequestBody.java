package webserver.http11.request;

import util.HttpRequestUtils;

import java.util.Map;

public class HttpRequestBody {
    private final String body;
    private final Map<String, String> parameters;

    public HttpRequestBody(String body, String contentType) {
        this.body = body;

        if (contentType.equals("application/x-www-form-urlencoded")) {
            this.parameters = HttpRequestUtils.parseQueryString(body);
        } else {
            this.parameters = Map.of();
        }
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "RequestBody [body=" + body + ", parameters=" + parameters + "]";
    }
}
