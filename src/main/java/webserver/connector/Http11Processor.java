package webserver.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.container.StandardContext;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;
import webserver.http11.session.HttpSession;

public class Http11Processor {
    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final CoyoteAdapter adapter;

    public Http11Processor(StandardContext context) {
        this.adapter = new CoyoteAdapter(context);
    }

    public void process(HttpRequest request, HttpResponse response) {
        log.info("Http11Processor processing request");
        try {
            adapter.service(request, response);
            handleSession(request, response);
        } catch (Exception e) {
            log.error("Error processing request", e);
            response.setStatusCode(500);
            response.setBody("Internal Server Error".getBytes());
        }
    }

    private void handleSession(HttpRequest request, HttpResponse response) {
        // 세션이 실제 생성된 경우에만 Set-Cookie 내려줌
        HttpSession session = request.getSession(false);
        if (session != null && request.isNewSession()) {
            response.addHeader("Set-Cookie", "JSESSIONID=" + session.getId() + "; Path=/; HttpOnly");
        }
    }
}
