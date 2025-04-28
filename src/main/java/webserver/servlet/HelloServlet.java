package webserver.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;

import java.io.IOException;

/**
 * 간단한 테스트 서블릿 클래스입니다.
 */
public class HelloServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(HelloServlet.class);

    @Override
    public void init() {
        log.info("HelloServlet initialized");
    }

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) throws IOException {
        log.info("Hello World");
        log.info("항상 같은 객체인지 ? : {}", this);
    }

    @Override
    public void destroy() {
        log.info("HelloServlet destroyed");
    }
}
