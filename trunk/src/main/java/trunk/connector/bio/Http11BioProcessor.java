package trunk.connector.bio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import trunk.connector.Http11Processor;
import trunk.container.StandardContext;
import trunk.http11.BioHttpRequestParser;
import trunk.http11.request.HttpRequest;
import trunk.http11.response.HttpResponse;
import trunk.http11.response.ResponseSender;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Http11BioProcessor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Http11BioProcessor.class);

    private final Socket socket;
    private final StandardContext standardContext;

    public Http11BioProcessor(Socket socket, StandardContext standardContext) {
        this.socket = socket;
        this.standardContext = standardContext;
    }

    @Override
    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", socket.getInetAddress(),
                socket.getPort());

        try (InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream();
             DataOutputStream dos = new DataOutputStream(out)) {

            // keep alive 지원
            while (!socket.isClosed()) {
                socket.setSoTimeout(1000); // 1초 타임아웃 설정

                // HTTP 요청 파싱
                HttpRequest request = BioHttpRequestParser.parse(in);

                if (request == null) {
                    continue;
                }

                HttpResponse response = new HttpResponse();

                Http11Processor processor = new Http11Processor(standardContext);
                processor.process(request, response);

                if (request.isKeepAlive()) {
                    response.addHeader("Connection", "keep-alive");
                    ResponseSender.sendResponseBIO(response, dos);
                } else {
                    response.addHeader("Connection", "close");
                    ResponseSender.sendResponseBIO(response, dos);
                    break;
                }
            }
        } catch (SocketTimeoutException e) {
            log.info("Keep-Alive timeout, closing connection");
        } catch (Exception e) {
            log.error("Error processing request", e);
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                log.error("Error closing connection", e);
            }
        }
    }

}