package webserver.connector.nio;

import webserver.connector.protocol.AbstractProtocol;

/**
 * {@code Http11NioProtocol}은 HTTP/1.1 + NIO 기반 {@code ProtocolHandler} 구현체입니다.
 * <ul>
 *   <li>{@code NioEndpoint} 인스턴스를 생성·보유한다.</li>
 *   <li>{@link AbstractProtocol} 에서 제공하는 템플릿 메서드 훅을 통해
 *       NIO 전용 소켓/스레드 설정을 주입한다.</li>
 *   <li>그 외 라이프사이클(init, start, stop, destory)는 상위 클래스가 처리한다.</li>
 * </ul>
 */
public class Http11NioProtocol extends AbstractProtocol {

    public Http11NioProtocol(int port) {
        super(port);
        this.endpoint = new NioEndpoint();
    }

    @Override
    protected void initInternal() throws Exception {

    }

    @Override
    protected void startInternal() throws Exception {

    }

    @Override
    protected void stopInternal() throws Exception {

    }

    @Override
    protected void destroyInternal() throws Exception {

    }
}
