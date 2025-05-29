package webserver.connector.bio;

import webserver.connector.ProtocolHandler;
import webserver.container.StandardContext;

public class Http11BioProtocol implements ProtocolHandler {
    private final BioEndpoint endpoint;

    public Http11BioProtocol(StandardContext context, int port) {
        this.endpoint = new BioEndpoint(context, port);
    }

    @Override
    public void start() {
        endpoint.start();
    }

}
