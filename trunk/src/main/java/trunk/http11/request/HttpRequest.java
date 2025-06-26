package trunk.http11.request;

import trunk.http11.session.HttpSession;
import trunk.http11.session.SessionManager;

import java.util.Map;
import java.util.UUID;

public class HttpRequest {
    private final HttpRequestStartLine startLine;
    private final HttpRequestHeader headers;
    private final HttpRequestBody body;
    private final Map<String, String> queryParameters;

    private HttpCookie cookies;
    private HttpSession session;
    private boolean isNewSession = false;

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
        if (cookies == null) {
            cookies = new HttpCookie(getHeaders().getHeaders().get("Cookie"));
        }
        return cookies;
    }

    public HttpSession getSession() {
        return getSession(true);
    }

    public HttpSession getSession(boolean create) {
        if (session == null) {
            String sessionId = getCookies().getCookie("JSESSIONID");
            if (sessionId == null && !create) {
                return null;
            }

            if (sessionId == null) {
                sessionId = UUID.randomUUID().toString();
                isNewSession = true;
            }

            session = SessionManager.getSession(sessionId, create);
        }
        return session;
    }

    public boolean isNewSession() {
        return isNewSession;
    }

    public boolean isKeepAlive() {
        String connection = getHeaders().getHeaders().get("Connection");
        return connection != null && connection.equalsIgnoreCase("keep-alive");
    }

    @Override
    public String toString() {
        return "HttpRequest [startLine=" + startLine + ", header=" + headers + ", body=" + body + "]";
    }
}
