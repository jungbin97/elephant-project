package webserver.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.container.StandardContext;

import java.io.IOException;

public class BioConnector {
    private static final Logger log = LoggerFactory.getLogger(BioConnector.class);
    private final ProtocolHandler protocolHandler;

    public BioConnector(StandardContext context, int port) {
        this.protocolHandler = new BioProtocolHandler(context, port);
    }

    public void start() throws IOException {
        log.info("Starting BioConnector...");
        protocolHandler.start();
    }
}
