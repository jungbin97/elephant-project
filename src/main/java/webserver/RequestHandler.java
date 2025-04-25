package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.container.Mapper;
import webserver.container.StandardContext;
import webserver.container.StandardWrapper;
import webserver.http11.HttpRequestParser;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;
import webserver.http11.session.HttpSession;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RequestHandler extends Thread {
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
            // HTTP 요청 파싱
            HttpRequest request = HttpRequestParser.parse(in);
            HttpResponse response = new HttpResponse();

            // 요청 URI에 해당하는 서블릿을 찾기 위해 매핑을 확인
            Mapper mapper = standardContext.getMapper();
            StandardWrapper wrapper = mapper.getStandardWrapper(request.getStartLine().getRequestUri());

            if (wrapper != null) {
                wrapper.service(request, response);
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

}