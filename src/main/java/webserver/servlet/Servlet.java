package webserver.servlet;

import webserver.container.StandardWrapper;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;

import java.io.IOException;

/**
 * 모든 서블릿 클래스가 구현해야 하는 핵심적인 메서드를 정의하는 인터페이스입니다.
 * <p>
 * 이 인터페이스는 서블릿과 서블릿 컨테이너({@link StandardWrapper}) 사이의 생명주기(lifecycle)
 * 계약을 정의합니다. 컨테이너는 이 인터페이스의 메서드들을 정해진 시점에 호출하여
 * 서블릿의 생성, 서비스, 소멸을 관리합니다.
 * <p>
 * 일반적으로 개발자는 이 인터페이스를 직접 구현하기보다, HTTP 프로토콜에 특화된
 * 추상 클래스인 {@link HttpServlet}을 상속받아 사용합니다.
 *
 * @author jungbin97
 * @see HttpServlet
 * @see StandardWrapper
 */
public interface Servlet {
    /**
     * 서블릿 컨테이너가 서블릿을 초기화하기 위해 단 한 번 호출하는 메서드입니다.
     * <p>
     * 이 메서드는 {@link #service(HttpRequest, HttpResponse)} 메서드가 호출되기 전에
     * 반드시 성공적으로 완료되어야 합니다. 서블릿이 필요로 하는 자원을 미리 준비하거나
     * 설정 파일을 읽는 등의 일회성 초기화 작업을 이 곳에서 수행할 수 있습니다.
     */
    void init();

    /**
     * 클라이언트의 요청을 받아 처리하고 응답을 생성하기 위해 서블릿 컨테이너에 의해 호출됩니다.
     * <p>
     * 이 메서드는 이 서블릿에 매핑된 모든 요청에 대해 반복적으로 호출됩니다.
     *
     * @param request  클라이언트의 요청 정보를 담고 있는 {@link HttpRequest} 객체
     * @param response 클라이언트에게 보낼 응답을 생성하기 위한 {@link HttpResponse} 객체
     * @throws IOException 요청 처리 또는 응답 생성 중 I/O 관련 문제가 발생할 경우
     */
    void service(HttpRequest request, HttpResponse response) throws IOException;

    /**
     * 서블릿 인스턴스가 컨테이너에서 제거되기 직전, 정리 작업을 수행하기 위해 단 한 번 호출됩니다.
     * <p>
     * 이 메서드가 호출된 후에는, 더 이상 이 서블릿으로 요청이 전달되지 않습니다.
     */
    void destroy();
}
