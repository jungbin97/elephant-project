package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.connector.BioConnector;
import webserver.container.ContextConfig;
import webserver.container.StandardContext;

public class WebServer {
    private static final Logger log = LoggerFactory.getLogger(WebServer.class);
    private static final int DEFAULT_PORT = 8080;
    public static final String WEBAPP_WEB_INF_WEB_XML = "webapp/WEB-INF/web.xml";

    public static void main(String[] args) throws Exception {
        int port = 0;
        if (args == null || args.length == 0) {
            port = DEFAULT_PORT;
        } else {
            port = Integer.parseInt(args[0]);
        }

        StandardContext standardContext = new StandardContext();
        // 기본 서블릿 등록: web.xml에 등록된 서블릿 등록
        ContextConfig contextConfig = new ContextConfig(standardContext);
        contextConfig.parseWebXml(WEBAPP_WEB_INF_WEB_XML);

        // eager 초기화
        standardContext.loadOnStartup();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Web Application Server shutdown.");
            standardContext.destroyAll();
        }));

        BioConnector connector = new BioConnector(standardContext, port);
        connector.start();

        log.info("Web Application Server started.");
    }
}
