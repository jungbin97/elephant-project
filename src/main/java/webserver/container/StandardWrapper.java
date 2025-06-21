package webserver.container;

import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;
import webserver.servlet.HttpServlet;
import webserver.servlet.Servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * 단일 서블릿 인스턴스의 생명주기를 관리하는 래퍼(Wrapper) 클래스입니다.
 * 톰캣의 StandardWrapper와 유사한 책임을 가집니다.
 * <p>
 * 이 클래스는 서블릿 클래스를 직접 인스턴스화하고, `init()`, `service()`, `destroy()`와 같은
 * 생명주기 메서드를 호출하는 역할을 담당합니다. 또한, 서블릿이 최초 요청을 받을 때 인스턴스를 생성하는
 * '지연 로딩(Lazy Loading)' 메커니즘을 지원합니다.
 *
 * @author jungbin97
 * @see Servlet
 * @see StandardContext
 */
public class StandardWrapper {
    private final Class<? extends Servlet> servletClass;
    private HttpServlet instance;
    private boolean instanceInitialized = false;
    private StandardContext context;

    public StandardWrapper(Class<? extends Servlet> servletClass, StandardContext context) {
        this.servletClass = servletClass;
        this.context = context;
    }

    /**
     * 래핑된 서블릿의 인스턴스를 생성하고 초기화합니다.
     * <p>
     * 리플렉션을 사용하여 서블릿의 기본 생성자를 호출해 인스턴스를 생성합니다.
     * 만약 서블릿이 {@link ServletContextAware}를 구현했다면, 부모 컨텍스트를 주입합니다.
     * 마지막으로 서블릿의 {@code init()} 메서드를 호출하여 초기화를 완료합니다.
     * 인스턴스가 이미 초기화된 경우에는 아무 작업도 수행하지 않습니다.
     *
     * @throws RuntimeException 서블릿 인스턴스 생성 또는 초기화 중 오류 발생 시
     */
    public void load() {
        if (instanceInitialized) {
            return;
        }

        try {
            instance = (HttpServlet) servletClass.getDeclaredConstructor().newInstance();

            // ContextAware 인터페이스를 구현한 경우, 컨텍스트를 설정합니다.
            if (instance instanceof ServletContextAware) {
                ((ServletContextAware) instance).setServletContext(context);
            }

            instance.init();
            instanceInitialized = true;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to load servlet: " + servletClass.getName(), e);
        }
    }

    /**
     * 요청을 처리하기 위해 서블릿의 {@code service()} 메서드를 호출합니다.
     * <p>
     * 서블릿 인스턴스가 아직 초기화되지 않았다면, 먼저 {@link #load()} 메서드를 호출하여
     * 인스턴스를 생성하고 초기화합니다(지연 로딩).
     *
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @throws IOException 서블릿 서비스 중 I/O 오류 발생 시
     */

    public void service(HttpRequest request, HttpResponse response) throws IOException {
        if (!instanceInitialized) {
            load();
        }
        instance.service(request, response);
    }

    /**
     * 서블릿 인스턴스를 소멸시키고 리소스를 해제하기 위해 {@code destroy()} 메서드를 호출합니다.
     * 인스턴스가 초기화된 상태일 때만 호출됩니다.
     */
    public void destroy() {
        if (instanceInitialized &&  instance != null) {
            instance.destroy();
        }
    }

    /**
     * 이 래퍼가 감싸고 있는 서블릿의 {@link Class} 객체를 반환합니다.
     *
     * @return 래핑된 서블릿의 클래스 객체
     */
    public Class<? extends Servlet> getInstance() {
        return servletClass;
    }
}

