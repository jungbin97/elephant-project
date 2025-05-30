package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.connector.Connector;
import webserver.connector.bio.Http11BioProtocol;
import webserver.connector.protocol.ProtocolHandler;
import webserver.container.ContextConfig;
import webserver.container.StandardContext;

public class WebServer {
    private static final Logger log = LoggerFactory.getLogger(WebServer.class);
    private static final int DEFAULT_PORT = 8080;
    public static final String WEBAPP_WEB_INF_WEB_XML = "webapp/WEB-INF/web.xml";

    public static void main(String[] args) throws Exception {
        // 1. Context 생성
        StandardContext standardContext = new StandardContext();
        // 기본 서블릿 등록: web.xml에 등록된 서블릿 등록
        ContextConfig contextConfig = new ContextConfig(standardContext);
        contextConfig.parseWebXml(WEBAPP_WEB_INF_WEB_XML);

        // eager 초기화
        standardContext.loadOnStartup();

        // 2. 프로토콜 핸들러 선택
        ProtocolHandler handler = new Http11BioProtocol(DEFAULT_PORT);

        // 3. Connector 구성
        Connector connector = new Connector(handler, standardContext);

        // 4. Lifecycle 실행
        log.info("Web Application Server started.");
        log.info("""
                
                 _____   _                  _                       _  \s
                |  ___| | |                | |                     | | \s
                | |__   | |   ___   _ __   | |__     __ _   _ __   | |_\s
                |  __|  | |  / _ \\ | '_ \\  | '_ \\   / _` | | '_ \\  | __|
                | |___  | | |  __/ | |_) | | | | | | (_| | | | | | | |_\s
                \\____/  |_|  \\___| | .__/  |_| |_|  \\__,_| |_| |_|  \\__|
                                   | |                                 \s
                                   |_|                                 \s
                """);
        connector.init();
        connector.start();

        // 종료 hook 등록
        // 추후 graceful shutdown을 위해서 사용할 수 있음.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Web Application Server shutdown.");
            try {
                standardContext.destroyAll();
                connector.stop();
            } catch (Exception e) {
                log.error("Error stopping Web Application Server", e);
            }
        }));
    }
}
