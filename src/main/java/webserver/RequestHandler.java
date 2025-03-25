package webserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.http11.response.HttpResponse;
import webserver.processor.RequestDispatcher;
import webserver.http11.HttpRequestParser;
import webserver.http11.request.HttpRequest;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;
    private final RequestDispatcher requestDispatcher;

    public RequestHandler(Socket connectionSocket, RequestDispatcher requestDispatcher) {
        this.connection = connectionSocket;
        this.requestDispatcher = requestDispatcher;
    }

    @Override
    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream();
             OutputStream out = connection.getOutputStream();
             DataOutputStream dos = new DataOutputStream(out)) {
            // HTTP 요청 파싱
            HttpRequest request = HttpRequestParser.parse(in);
            HttpResponse response = requestDispatcher.dispatch(request);

            response.sendResponse(dos);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}