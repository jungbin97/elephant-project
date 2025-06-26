package trunk.connector.protocol;

import trunk.container.StandardContext;

public interface ProtocolHandler {
    void initProtocol() throws Exception;
    void startProtocol() throws Exception;
    void stopProtocol() throws Exception;
    void destroyProtocol() throws Exception;

    void setContext(StandardContext context);
}
