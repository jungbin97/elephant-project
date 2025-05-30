package webserver.connector.endpoint;

import webserver.container.StandardContext;

public interface Endpoint {
    void bind(int port) throws Exception;
    void startEndpoint(StandardContext context) throws Exception;
    void stopEndpoint() throws Exception;
}
