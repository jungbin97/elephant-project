package trunk.connector.bio;

import trunk.connector.endpoint.AbstractEndpoint;
import trunk.container.StandardContext;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BioEndpoint extends AbstractEndpoint {
    private ExecutorService executorService;
    private ServerSocket serverSocket;

    @Override
    protected void bindInternal() throws Exception {
        serverSocket = new ServerSocket(port);
        executorService = Executors.newFixedThreadPool(200);
    }

    @Override
    protected void startInternal(StandardContext context) throws Exception {
        Thread acceptor = new Thread(new BioAcceptor(serverSocket, executorService, context));
        acceptor.start();
    }

    @Override
    protected void stopInternal() throws Exception {
        serverSocket.close();
        executorService.shutdown();
    }
}
