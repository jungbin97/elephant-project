package webserver.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.connector.bio.Http11BioProtocol;
import webserver.container.StandardContext;
import webserver.lifecycle.Lifecycle;

import java.io.IOException;

/**
 * {@code Connector}는 클라이언트의 요청에 대한 진입점을 제공합니다.
 * <p>
 * <h2>기능</h2>
 * <ul>
 *     <li>클라이언트의 연결을 수신하고, 요청을 처리하기 위한 {@code ProtocolHandler}를 시작합니다.</li>
 *     <li>포트 번호를 통해 서버와 클라이언트 간의 통신을 설정합니다.</li>
 *     <li> context를 통해 서블릿 컨텍스트와 연동됩니다.</li>
 * </ul>
 *
 * @see ProtocolHandler
 * @see Http11BioProtocol
 * @see Http11NioProtocol
 */
public class Connector implements Lifecycle {
    private static final Logger log = LoggerFactory.getLogger(Connector.class);

    private final ProtocolHandler protocolHandler;

    public Connector(StandardContext context, int port) {
        this.protocolHandler = new Http11BioProtocol(context, port);
    }

    @Override
    public void init() throws Exception {

    }

    public void start() throws IOException {
        log.info("Starting BioConnector...");
        protocolHandler.start();
    }

    @Override
    public void stop() throws Exception {

    }

    @Override
    public void destroy() throws Exception {

    }
}
