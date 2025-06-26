package trunk.connector.endpoint;

import trunk.container.StandardContext;

public interface Endpoint {
    void bind(int port) throws Exception;
    void startEndpoint(StandardContext context) throws Exception;
    void stopEndpoint() throws Exception;
}
