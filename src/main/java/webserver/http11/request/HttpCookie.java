package webserver.http11.request;

import util.HttpRequestUtils;

import java.util.Map;

public class HttpCookie {
    private final Map<String, String> cookies;

    public HttpCookie(String cookieValue) {
        this.cookies = HttpRequestUtils.parseCookies(cookieValue);
    }

    public String getCookie(String key) {
        return cookies.get(key);
    }
}
