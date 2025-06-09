package webserver.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.connector.bio.Http11BioProtocol;
import webserver.connector.protocol.ProtocolHandler;
import webserver.container.StandardContext;
import webserver.lifecycle.Lifecycle;

/**
 * {@code Connector}는 클라이언트의 요청에 대한 진입점을 제공합니다.
 * <p>
 * <h2>기능</h2>
 * <ul>
 *     <li>클라이언트의 연결을 수신하고, 요청을 처리하기 위한 {@code ProtocolHandler}를 시작합니다.</li>
 *     <li>ProtocolHandler에 연결된 context를 설정합니다.</li>
 *     <li>lifecycle 메서드를 통해 초기화, 시작, 중지 및 소멸 작업을 수행합니다.</li>
 * </ul>
 *
 * @see ProtocolHandler
 * @see Http11BioProtocol
 */
public class Connector implements Lifecycle {
    private static final Logger log = LoggerFactory.getLogger(Connector.class);

    private final ProtocolHandler protocolHandler;

    public Connector(ProtocolHandler protocolHandler, StandardContext context) {
        this.protocolHandler = protocolHandler;
        this.protocolHandler.setContext(context);
    }

    @Override
    public void init() throws Exception {
        log.info("Initializing ProtocolHandler...");
        protocolHandler.initProtocol();
    }

    public void start() throws Exception {
        log.info("Starting ProtocolHandler...");
        protocolHandler.startProtocol();
    }

    @Override
    public void stop() throws Exception {
        log.info("Stopping ProtocolHandler...");
        protocolHandler.stopProtocol();
    }

    @Override
    public void destroy() throws Exception {

    }
}
