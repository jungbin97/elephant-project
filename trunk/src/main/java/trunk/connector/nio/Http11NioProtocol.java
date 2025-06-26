package trunk.connector.nio;

import trunk.connector.protocol.AbstractProtocol;

/**
 * HTTP/1.1 프로토콜을 Non-blocking I/O(NIO) 방식으로 처리하는 {@link trunk.connector.protocol.ProtocolHandler} 구현체입니다.
 * <p>
 * 이 클래스는 특정 프로토콜을 처리하기 위한 구체적인 컴포넌트들을 조합하는 역할을 합니다.
 * 톰캣의 Connector가 사용하는 ProtocolHandler와 유사한 구조를 가집니다.
 *
 * <h2>주요 역할</h2>
 * <ul>
 * <li>NIO 통신을 담당하는 {@link NioEndpoint} 인스턴스를 생성하고 생명주기를 위임합니다.</li>
 * <li>상위 클래스인 {@link AbstractProtocol}의 템플릿 메서드 패턴을 통해,
 * 서버의 생명주기(init, start, stop, destroy)가 {@link NioEndpoint}에 올바르게 전파되도록 합니다.</li>
 * </ul>
 *
 * @author jungbin97
 * @see AbstractProtocol
 * @see NioEndpoint
 */
public class Http11NioProtocol extends AbstractProtocol {

    /**
     * 지정된 포트에서 NIO 기반 HTTP/1.1 프로토콜 핸들러를 생성합니다.
     * 생성 시, 통신 종단점(Endpoint)으로 {@link NioEndpoint}를 설정합니다.
     *
     * @param port 서버가 리스닝할 포트 번호
     */
    public Http11NioProtocol(int port) {
        super(port);
        this.endpoint = new NioEndpoint();
    }

    /**
     * 프로토콜 핸들러 레벨의 추가적인 초기화 작업을 수행합니다.
     * <p>
     * 현재 구현에서는 모든 초기화 로직이 {@link NioEndpoint}에 위임되어 있으므로 별도 작업이 필요하지 않습니다.
     */
    @Override
    protected void initInternal() throws Exception {

    }

    /**
     * 프로토콜 핸들러 레벨의 추가적인 시작 작업을 수행합니다.
     * <p>
     * 현재 구현에서는 모든 시작 로직이 {@link NioEndpoint}에 위임되어 있으므로 별도 작업이 필요하지 않습니다.
     */
    @Override
    protected void startInternal() throws Exception {

    }

    /**
     * 프로토콜 핸들러 레벨의 추가적인 중지 작업을 수행합니다.
     * <p>
     * 현재 구현에서는 모든 중지 로직이 {@link NioEndpoint}에 위임되어 있으므로 별도 작업이 필요하지 않습니다.
     */
    @Override
    protected void stopInternal() throws Exception {

    }

    /**
     * 프로토콜 핸들러 레벨의 추가적인 소멸 작업을 수행합니다.
     * <p>
     * 현재 구현에서는 모든 소멸 로직이 {@link NioEndpoint}에 위임되어 있으므로 별도 작업이 필요하지 않습니다.
     */
    @Override
    protected void destroyInternal() throws Exception {

    }
}
