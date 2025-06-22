package webserver.servlet;

import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;

import java.io.IOException;

/**
 * HTTP 프로토콜을 처리하는 서블릿을 쉽게 구현할 수 있도록 돕는 추상 클래스입니다.
 * <p>
 * 이 클래스는 {@link Servlet} 인터페이스를 구현하며, 모든 요청을 처리하는 {@link #service(HttpRequest, HttpResponse)}
 * 메서드의 기본 로직을 제공합니다. {@code service} 메서드는 요청의 HTTP 메서드(GET, POST 등)를 확인하고,
 * 그에 맞는 {@code doXxx()} 형태의 메서드(예: {@link #doGet}, {@link #doPost})를 호출해주는
 * '디스패치' 역할을 수행합니다.
 * <p>
 * 따라서 이 클래스를 상속받는 개발자는 {@code service} 메서드를 직접 오버라이드할 필요 없이,
 * 처리하고자 하는 특정 HTTP 메서드에 해당하는 {@code doXxx()} 메서드만 구현하면 됩니다.
 *
 * @author jungbin97
 * @see Servlet
 */
public abstract class HttpServlet implements Servlet {

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    /**
     * 모든 HTTP 요청에 대한 진입점 역할을 합니다.
     * <p>
     * 요청 객체에서 HTTP 메서드를 추출하여, 해당하는 {@code doXxx()} 메서드로 요청을 전달합니다.
     * 예를 들어, GET 요청은 {@link #doGet(HttpRequest, HttpResponse)}으로 전달됩니다.
     * 지원하지 않는 HTTP 메서드로 요청이 들어오면 405 Method Not Allowed 응답을 보냅니다.
     *
     * @param request  클라이언트의 요청 객체
     * @param response 클라이언트에게 보낼 응답 객체
     * @throws IOException 요청 처리 중 I/O 오류 발생 시
     */
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
