package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.connector.Http11Processor;
import webserver.container.StandardContext;
import webserver.http11.HttpRequestParser;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class RequestHandler implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;
    private final StandardContext standardContext;

    public RequestHandler(Socket connectionSocket, StandardContext standardContext) {
        this.connection = connectionSocket;
        this.standardContext = standardContext;
    }

    @Override
    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream();
             OutputStream out = connection.getOutputStream();
             DataOutputStream dos = new DataOutputStream(out)) {

            // keep alive 지원
            while (!connection.isClosed()) {
                connection.setSoTimeout(1000); // 1초 타임아웃 설정

                // HTTP 요청 파싱
                HttpRequest request = HttpRequestParser.parse(in);

                if (request == null) {
                    continue;
                }

                HttpResponse response = new HttpResponse();

                Http11Processor processor = new Http11Processor(standardContext);
                processor.process(request, response);

                if (request.isKeepAlive()) {
                    response.addHeader("Connection", "keep-alive");
                    response.sendResponse(dos);
                } else {
                    response.addHeader("Connection", "close");
                    response.sendResponse(dos);
                    break;
                }
            }
        } catch (SocketTimeoutException e) {
            log.info("Keep-Alive timeout, closing connection");
        } catch (Exception e) {
            log.error("Error processing request", e);
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                log.error("Error closing connection", e);
            }
        }
    }

}