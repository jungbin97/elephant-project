package webserver.connector.bio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.connector.RequestHandler;
import webserver.container.StandardContext;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class BioAcceptor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(BioAcceptor.class);

    private final ServerSocket serverSocket;
    private final ExecutorService executorService;
    private final StandardContext context;

    public BioAcceptor(ServerSocket serverSocket, ExecutorService executorService, StandardContext context) {
        this.serverSocket = serverSocket;
        this.executorService = executorService;
        this.context = context;
    }

    @Override
    public void run() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                executorService.submit(new RequestHandler(socket, context));
            } catch (IOException e) {
                // 로그에 연결 수락 중 오류를 기록
                log.error("Error accepting connection: {}", e.getMessage());
            }
        }
    }
}

