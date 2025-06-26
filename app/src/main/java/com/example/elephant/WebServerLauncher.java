package com.example.elephant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import trunk.connector.Connector;
import trunk.connector.nio.Http11NioProtocol;
import trunk.connector.protocol.ProtocolHandler;
import trunk.container.ContextConfig;
import trunk.container.StandardContext;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Elephant 웹 서버의 실제 부팅 로직을 담당하는 클래스입니다.
 * 서버의 컴포넌트를 생성, 설정, 조립하고 생명주기를 관리합니다.
 */
public class WebServerLauncher {
    private static final Logger log = LoggerFactory.getLogger(WebServerLauncher.class);
    private static final int DEFAULT_PORT = 8080;

    private Connector connector;

    public void start() {
        try {
            StandardContext context = configureContext();
            ProtocolHandler handler = new Http11NioProtocol(DEFAULT_PORT);

            this.connector = new Connector(handler, context);
            this.connector.init();
            this.connector.start();

            logAsciiArt();
            log.info("Web Application Server started successfully on port {}.", DEFAULT_PORT);

            setupShutdownHook(context);

        } catch (Exception e) {
            log.error("Failed to start the web server", e);
            // 서버 시작 실패 시 애플리케이션을 종료
            System.exit(1);
        }
    }

    private StandardContext configureContext() throws URISyntaxException {
        StandardContext context = new StandardContext();

        // webapp 디렉토리의 실제 경로 찾아 Context의 dcosBase 설정
        URL webappUrl = this.getClass().getClassLoader().getResource("webapp");
        if (webappUrl == null) {
            throw new IllegalStateException("Could not find webapp directory in classpath!");
        }
        String webappPath = new File(webappUrl.toURI()).getAbsolutePath();
        context.setDocBase(webappPath);

        // web.xml 파일 경로 찾아 파싱
        File webXmlFile = new File(webappPath, "WEB-INF/web.xml");
        if (!webXmlFile.exists()) {
            throw new IllegalStateException("Could not find 'web.xml' in 'webapp/WEB-INF/'.");
        }

        ContextConfig contextConfig = new ContextConfig(context);
        contextConfig.parseWebXml(webXmlFile.getAbsolutePath());

        // 서블릿 즉시 로딩
        context.loadOnStartup();

        return context;
    }

    /**
     * 애플리케이션 종료 시 안전하게 서버 자원을 해제하기 위한 종료 Hook을 등록합니다.
     */
    private void setupShutdownHook(StandardContext context) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.info("Shutting down the web application server...");
                context.destroyAll();
                connector.stop();
            } catch (Exception e) {
                log.error("Error stopping Web Application Server", e);
            }
            log.info("Server has been shut down gracefully.");
        }));
    }

    private void logAsciiArt() {
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
    }
}
