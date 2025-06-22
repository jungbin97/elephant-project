package webserver.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.servlet.Servlet;

import java.util.*;

/**
 * 하나의 웹 애플리케이션 컨텍스트를 나타내는 클래스. 톰캣의 StandardContext와 유사한 역할을 합니다.
 * <p>
 * 이 클래스는 특정 웹 애플리케이션에 속한 모든 서블릿의 생명주기를 관리하고,
 * 요청을 적절한 서블릿으로 라우팅하는 {@link Mapper}를 소유하고 초기화합니다.
 * <h2>주요 책임</h2>
 * <ul>
 * <li>서블릿 래퍼({@link StandardWrapper})들을 컨테이너로 관리합니다.</li>
 * <li>서버 시작 시 `load-on-startup` 값이 0 이상인 서블릿을 미리 로드(Eager Loading)합니다.</li>
 * <li>서버 종료 시 등록된 모든 서블릿의 `destroy` 메서드를 호출하여 리소스를 해제합니다.</li>
 * <li>URL 패턴과 서블릿을 매핑하는 {@link Mapper}를 생성하고 관리합니다.</li>
 * </ul>
 *
 * @author jungbin97
 * @see StandardWrapper
 * @see Mapper
 * @see ContextConfig
 */
public class StandardContext {
    private static final Logger log = LoggerFactory.getLogger(StandardContext.class);
    private final Map<String, StandardWrapper> children = new HashMap<>();
    private final List<StandardWrapper> loadOnStartupWrappers = new ArrayList<>();
    private Mapper mapper;

    /**
     * 새로운 서블릿을 이 컨텍스트에 등록합니다.
     * <p>
     * 이 메서드는 주로 {@link ContextConfig}가 web.xml을 파싱한 후 호출하여,
     * 서블릿 정보를 컨텍스트에 추가하는 데 사용됩니다.
     *
     * @param urlPattern    이 서블릿과 매핑될 URL 패턴
     * @param servletClass  등록할 서블릿의 클래스 타입
     * @param loadOnStartup 서블릿 로딩 시점을 결정하는 값. 0 이상이면 서버 시작 시 로드됩니다.
     */
    public void addChild(String urlPattern, Class<? extends Servlet> servletClass, int loadOnStartup) {
        StandardWrapper wrapper = new StandardWrapper(servletClass, this);
        children.put(urlPattern, wrapper);

        if (loadOnStartup >= 0) {
            loadOnStartupWrappers.add(wrapper);
        }
    }

    /**
     * `load-on-startup` 값이 0 또는 양수인 서블릿들을 즉시 로드하고,
     * 모든 서블릿 정보가 등록된 후 {@link Mapper}를 초기화합니다.
     * <p>
     * 이 메서드는 웹 애플리케이션이 시작될 때 호출되어야 합니다.
     */
    public void loadOnStartup() {
        log.info("eager loading servlets");
        for (StandardWrapper wrapper : loadOnStartupWrappers) {
            wrapper.load();
        }
        this.mapper = new Mapper(children);
    }

    /**
     * 이 컨텍스트에 등록된 모든 서블릿의 `destroy` 메서드를 호출합니다.
     * 웹 애플리케이션이 종료될 때 호출되어, 서블릿들이 사용하던 리소스를 안전하게 해제하도록 합니다.
     */
    public void destroyAll() {
        children.values().forEach(StandardWrapper::destroy);
    }

    /**
     * 이 컨텍스트에 등록된 모든 서블릿 매핑 정보를 반환합니다.
     * 반환된 맵은 수정할 수 없습니다.
     *
     * @return URL 패턴을 키로, 서블릿 래퍼를 값으로 갖는 수정 불가능한 맵
     */
    public Map<String, StandardWrapper> getServletMapings() {
        return Collections.unmodifiableMap(children);
    }

    /**
     * 이 컨텍스트의 요청 라우팅을 담당하는 {@link Mapper} 인스턴스를 반환합니다.
     *
     * @return 요청 URI 매핑을 처리하는 {@code Mapper}
     */
    public Mapper getMapper() {
        return mapper;
    }
}
