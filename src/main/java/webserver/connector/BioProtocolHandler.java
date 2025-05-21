package webserver.connector;

import webserver.container.StandardContext;

public class BioProtocolHandler implements ProtocolHandler {
    private final BioEndpoint endpoint;

    BioProtocolHandler(StandardContext context, int port) {
        this.endpoint = new BioEndpoint(context, port);
    }

    @Override
    public void start() {
        endpoint.start();
    }

}
