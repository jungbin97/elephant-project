package webserver;

import mvc.DispatcherServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.http11.HttpRequestParser;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;
import webserver.http11.session.HttpSession;
import webserver.staticresource.DefaultServlet;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;
    private final DispatcherServlet dispatcherServlet;
    private final DefaultServlet defaultServlet;

    public RequestHandler(Socket connectionSocket, DispatcherServlet dispatcherServlet, DefaultServlet defaultServlet) {
        this.connection = connectionSocket;
        this.dispatcherServlet = dispatcherServlet;
        this.defaultServlet = defaultServlet;
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
            HttpResponse response = new HttpResponse();

            if (isStaticResource(request)) {
                defaultServlet.service(request, response);
            } else {
                dispatcherServlet.service(request, response);
            }

            // 세션이 실제 생성된 경우에만 Set-Cookie 내려줌
            HttpSession session = request.getSession(false);
            if (session != null && request.isNewSession()) {
                response.addHeader("Set-Cookie", "JSESSIONID=" + session.getId() + "; Path=/; HttpOnly");
            }
            response.sendResponse(dos);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean isStaticResource(HttpRequest request) {
        String path = request.getStartLine().getRequestUri();
        return path.matches(".*\\.(html|css|js|png)$");
    }

}