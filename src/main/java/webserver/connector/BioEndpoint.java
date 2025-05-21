package webserver.connector;

import webserver.container.StandardContext;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BioEndpoint {
    private final ExecutorService executor = Executors.newFixedThreadPool(100);
    private final StandardContext context;
    private final int port;

    public BioEndpoint(StandardContext context, int port) {
        this.context = context;
        this.port = port;
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            BioConnectionHandler handler = new BioConnectionHandler(context);
            Thread acceptor = new Thread(new Acceptor(serverSocket, executor, handler));
            acceptor.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
