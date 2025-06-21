package webserver.container;

/**
 * 자신이 포함된 {@link StandardContext}에 접근해야 하는 서블릿이 구현해야 하는 인터페이스입니다.
 * <p>
 * 이 인터페이스는 일종의 콜백(callback)으로, 서블릿이 자신의 컨테이너(부모)인
 * {@code StandardContext}에 대한 참조를 주입(inject)받기 위해 사용됩니다.
 * <p>
 * 서블릿 컨테이너({@link StandardWrapper})는 서블릿을 초기화하는 과정에서,
 * 해당 서블릿이 이 인터페이스를 구현했는지 확인합니다. 만약 구현했다면,
 * {@link #setServletContext(StandardContext)} 메서드를 호출하여
 * 서블릿에게 부모 컨텍스트에 대한 참조를 전달합니다.
 *
 * @see StandardContext
 * @see StandardWrapper
 */
public interface ServletContextAware {
    /**
     * 서블릿이 자신의 부모인 {@link StandardContext}를 주입받기 위해 호출되는 메서드입니다.
     *
     * @param context 이 서블릿을 소유하고 있는 {@code StandardContext} 인스턴스
     */
    void setServletContext(StandardContext context);
}
