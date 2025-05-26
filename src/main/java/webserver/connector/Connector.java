package webserver.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.container.StandardContext;

import java.io.IOException;

public class Connector {
    private static final Logger log = LoggerFactory.getLogger(Connector.class);
    private final ProtocolHandler protocolHandler;

    public Connector(StandardContext context, int port) {
        this.protocolHandler = new BioProtocolHandler(context, port);
    }

    public void start() throws IOException {
        log.info("Starting BioConnector...");
        protocolHandler.start();
    }
}
