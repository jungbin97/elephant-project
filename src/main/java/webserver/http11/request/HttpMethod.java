package webserver.http11.request;

public enum HttpMethod {
    GET, POST, PUT, DELETE;

    public static HttpMethod of(String method) {
        for (HttpMethod httpMethod : values()) {
            if (httpMethod.name().equalsIgnoreCase(method)) {
                return httpMethod;
            }
        }
        throw new IllegalArgumentException("Invalid HTTP Method : " + method);
    }
}
