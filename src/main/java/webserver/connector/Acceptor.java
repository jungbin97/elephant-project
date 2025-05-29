package webserver.connector;

import webserver.connector.bio.BioConnectionHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;

public class Acceptor implements Runnable {
    private final ServerSocket serverSocket;
    private final Executor executor;
    private final BioConnectionHandler handler;

    public Acceptor(ServerSocket serverSocket, Executor executor, BioConnectionHandler handler) {
        this.serverSocket = serverSocket;
        this.executor = executor;
        this.handler = handler;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Socket socket = serverSocket.accept();
                executor.execute(() -> handler.process(socket));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

