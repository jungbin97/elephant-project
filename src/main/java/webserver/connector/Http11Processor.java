package webserver.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.container.StandardContext;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;
import webserver.http11.session.HttpSession;

/**
 * 파싱된 {@link HttpRequest}를 받아 실제 처리 로직을 수행하고 {@link HttpResponse}를 채우는 역할을 담당합니다.
 * <p>
 * 이 클래스는 특정 프로토콜(BIO/NIO)에 종속되지 않는, HTTP 요청 처리의 핵심 로직을 캡슐화합니다.
 * {@link CoyoteAdapter}를 통해 요청을 서블릿 컨테이너에 전달하고, 세션 관리와 같은
 * 요청 후처리 작업을 수행하며, 처리 과정에서 발생하는 모든 예외를 최종적으로 처리합니다.
 *
 * @author jungbin97
 * @see CoyoteAdapter
 * @see HttpRequest
 * @see HttpResponse
 */
public class Http11Processor {
    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final CoyoteAdapter adapter;

    /**
     * 지정된 서블릿 컨텍스트로 Http11Processor를 생성합니다.
     * 내부적으로 {@link CoyoteAdapter}를 생성하여 Connector와 Container를 연결할 준비를 합니다.
     *
     * @param context 요청을 전달할 웹 애플리케이션의 {@link StandardContext}
     */
    public Http11Processor(StandardContext context) {
        this.adapter = new CoyoteAdapter(context);
    }

    /**
     * 요청 처리의 전체 과정을 관장하는 메인 메서드입니다.
     * <p>
     * 1. {@link CoyoteAdapter#service(HttpRequest, HttpResponse)}를 호출하여 서블릿 실행을 위임합니다. <br>
     * 2. 서블릿 실행이 끝난 후, {@link #handleSession(HttpRequest, HttpResponse)}을 호출하여 세션 관련 후처리를 수행합니다. <br>
     * 3. 처리 과정 중 발생하는 모든 예외를 잡아 500 Internal Server Error 응답을 설정합니다.
     *
     * @param request  완전하게 파싱된 HTTP 요청 객체
     * @param response 서블릿 및 후처리기에서 채워나갈 HTTP 응답 객체
     */
    public void process(HttpRequest request, HttpResponse response) {
        log.info("Http11Processor processing request");
        try {
            adapter.service(request, response);
            handleSession(request, response);
        } catch (Exception e) {
            log.error("Error processing request", e);
            response.setStatusCode(500);
            response.setBody("Internal Server Error".getBytes());
        }
    }

    /**
     * 세션 관련 후처리 로직을 수행합니다.
     * <p>
     * 요청 처리 과정에서 새로운 세션이 생성된 경우에만, 클라이언트에게 세션 ID를 전달하기 위한
     * 'Set-Cookie' 헤더를 응답에 추가합니다.
     *
     * @param request  처리 완료된 HTTP 요청 객체
     * @param response 서블릿에 의해 채워진 HTTP 응답 객체
     */
    private void handleSession(HttpRequest request, HttpResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null && request.isNewSession()) {
            response.addHeader("Set-Cookie", "JSESSIONID=" + session.getId() + "; Path=/; HttpOnly");
        }
    }
}
