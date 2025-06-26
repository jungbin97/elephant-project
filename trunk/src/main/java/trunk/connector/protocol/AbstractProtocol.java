package trunk.connector.protocol;

import trunk.connector.endpoint.AbstractEndpoint;
import trunk.container.StandardContext;

/**
 * AbstractProtocol은 ProtocolHandler의 기본 구현을 제공합니다.
 * <p>
 * 이 클래스는 ProtocolHandler 인터페이스를 구현하며,
 * 포트 번호와 StandardContext를 관리합니다.
 * </p>
 */
public abstract class AbstractProtocol implements ProtocolHandler {
    protected StandardContext context;
    protected AbstractEndpoint endpoint;
    protected int port;

    protected AbstractProtocol(int port) {
        this.port = port;
    }

    @Override
    public void setContext(StandardContext context) {
        this.context = context;
    }

    @Override
    public final void initProtocol() throws Exception {
        endpoint.bind(port);
        initInternal();  // hook, 서브클래스에서 구현할 초기화 작업
    }

    @Override
    public final void startProtocol() throws Exception {
        endpoint.startEndpoint(context);
        startInternal();
    }

    @Override
    public final void stopProtocol() throws Exception {
        endpoint.stopEndpoint();
        stopInternal();
    }

    @Override
    public final void destroyProtocol() throws Exception {
        destroyInternal();
    }


    /* 서브클래스에서 구현해야 하는 hook 메소드들 */
    protected abstract void initInternal() throws Exception;
    protected abstract void startInternal() throws Exception;
    protected abstract void stopInternal() throws Exception;
    protected abstract void destroyInternal() throws Exception;
}
