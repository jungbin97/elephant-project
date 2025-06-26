package trunk.connector.endpoint;

import trunk.container.StandardContext;

public abstract class AbstractEndpoint implements Endpoint {
    protected int port;

    @Override
    public final void bind(int port) throws Exception {
        this.port = port;
        bindInternal();
    }

    @Override
    public final void startEndpoint(StandardContext context) throws Exception {
        startInternal(context);
    }

    @Override
    public final void stopEndpoint() throws Exception {
        stopInternal();
    }


    /* 서브클래스에서 구현해야 하는 hook 메소드들 */
    protected abstract void bindInternal() throws Exception;
    protected abstract void startInternal(StandardContext context) throws Exception;
    protected abstract void stopInternal() throws Exception;
}
