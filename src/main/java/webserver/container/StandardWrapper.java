package webserver.container;

import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;
import webserver.servlet.HttpServlet;
import webserver.servlet.Servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * {@code StandardWrapper} 클래스는 서블릿을 래핑하는 역할을 합니다.
 * 서블릿 인스턴스 로딩, 요청 처리 및 소멸을 담당합니다.
 * <p>
 */
public class StandardWrapper {
    private final Class<? extends Servlet> servletClass;
    private HttpServlet instance;
    private boolean instanceInitialized = false;

    public StandardWrapper(Class<? extends Servlet> servletClass) {
        this.servletClass = servletClass;
    }

    public void load() {
        if (instanceInitialized) {
            return;
        }

        try {
            instance = (HttpServlet) servletClass.getDeclaredConstructor().newInstance();
            instance.init();
            instanceInitialized = true;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to load servlet: " + servletClass.getName(), e);
        }
    }

    public void service(HttpRequest request, HttpResponse response) throws IOException {
        if (!instanceInitialized) {
            load();
        }
        instance.service(request, response);
    }

    public void destroy() {
        if (instanceInitialized &&  instance != null) {
            instance.destroy();
        }
    }

    public Class<? extends Servlet> getInstance() {
        return servletClass;
    }
}

