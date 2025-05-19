package webserver.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.servlet.HttpServlet;
import webserver.servlet.Servlet;

import java.util.*;

/**
 * {@code StandardContext}는 웹 애플리케이션의 내에서 서블릿의 관리 및 생명 주기를 담당합니다.
 * <p>
 * <h2>기능</h2>
 * <ul>
 *     <li>서블릿의 컨텍스트에 등록, 초기 로딩 및 소멸을 처리합니다.</li>
 * </ul>
 * @see StandardWrapper
 * @see HttpServlet
 * @see Mapper
 * @author  jungbin97
 */
public class StandardContext {
    private static final Logger log = LoggerFactory.getLogger(StandardContext.class);
    private final Map<String, StandardWrapper> children = new HashMap<>();
    private final List<StandardWrapper> loadOnStartupWrappers = new ArrayList<>();
    private Mapper mapper;

    /**
     *  이 컨텍스트 내에 새로운 서블릿을 등록합니다.
     * @param urlPattern 이 서블릿에 대한 URL 패턴입니다.
     * @param servletClass 등록될 서블릿 클래스입니다.
     * @param loadOnStartup 서블릿의 로딩 시점 을 결정하는 값입니다.
     *                      0 또는 양수인 경우, 서버 시작 시점에 로드됩니다.
     *                      음수인 경우, 서블릿 로드를 지연합니다.
     */
    public void addChild(String urlPattern, Class<? extends Servlet> servletClass, int loadOnStartup) {
        StandardWrapper wrapper = new StandardWrapper(servletClass, this);
        children.put(urlPattern, wrapper);

        if (loadOnStartup >= 0) {
            loadOnStartupWrappers.add(wrapper);
        }
    }

    /**
     *  웹 애플리케이션 시작 시점에 load-on-startup이 0 또는 양수인 서블릿을 로드합니다.
     *  loadOnStartup이 0 또는 양수인 서블릿만 로드됩니다.
     *  이 시점에 Mapper도 초기화합니다.
     */
    public void loadOnStartup() {
        log.info("eager loading servlets");
        for (StandardWrapper wrapper : loadOnStartupWrappers) {
            wrapper.load();
        }
        this.mapper = new Mapper(children);
    }

    /**
     *  이 컨텍스트 내에 등록된 모든 서블릿을 소멸합니다.
     * 이 메서드는 관리되는 모든 서블릿을 순회하며, 각 서블릿의 destroy 메서드를 호출하여
     * 보유하고 있을 수 있는 모든 리소스를 해제합니다.
     */
    public void destroyAll() {
        children.values().forEach(StandardWrapper::destroy);
    }

    /**
     *  이 컨텍스트 내에 등록된 모든 서블릿을 반환합니다.
     * @return 이 컨텍스트 내에 등록된 서블릿의 Map입니다.
     */
    public Map<String, StandardWrapper> getServletMapings() {
        return Collections.unmodifiableMap(children);
    }

    /**
     * 이 컨텍스트에 등록된 Mapper를 반환합니다.
     * @return 요청 URI 매핑을 처리하는 Mapper입니다.
     */
    public Mapper getMapper() {
        return mapper;
    }
}
