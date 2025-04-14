package webserver.servlet;

import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;

import java.io.IOException;

/**
 * Serlvet 인터페이스는 서블릿의 기본 동작을 정의합니다.
 * <p>
 * 서블릿을 구현하는 클래스는 이 인터페이스를 구현해야 하며,
 * init(), service(), destroy() 메서드를 오버라이드하여
 * 서블릿의 초기화, 요청 처리, 종료 작업을 수행합니다.
 */
public interface Servlet {
    void init();

    void service(HttpRequest request, HttpResponse response) throws IOException;

    void destroy();
}
