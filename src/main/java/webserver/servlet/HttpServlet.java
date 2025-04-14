package webserver.servlet;

import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;

import java.io.IOException;

/**
 * HttpServlet 클래스는 HTTP 요청을 처리하는 서블릿의 기본 클래스를 정의합니다.
 * <p>
 * 추상 클래스로, 서블릿을 구현하는 클래스는 이 클래스를 상속받아
 * doGet() 및 doPost() 메서드를 구현해야 합니다.
 */
public abstract class HttpServlet implements Servlet {

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        String method = request.getStartLine().getMethod();

        switch (method) {
            case "GET" -> doGet(request, response);
            case "POST" -> doPost(request, response);
            default -> {
                response.setStatusCode(405);
                response.setHeader("Content-Type", "text/plain");
                response.setBody("Method Not Allowed".getBytes());
            }
        }
    }

    protected void doGet(HttpRequest request, HttpResponse response) throws IOException {
        response.setStatusCode(405);
        response.setHeader("Content-Type", "text/plain");
        response.setBody("GET Allowed".getBytes());
    }

    protected void doPost(HttpRequest request, HttpResponse response) throws IOException {
        response.setStatusCode(405);
        response.setHeader("Content-Type", "text/plain");
        response.setBody("POST Not Allowed".getBytes());
    }
}
