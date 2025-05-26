package webserver.connector;

import webserver.container.Mapper;
import webserver.container.StandardContext;
import webserver.container.StandardWrapper;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;
import webserver.http11.session.HttpSession;

public class Http11Processor {
    private final StandardContext context;

    public Http11Processor(StandardContext context) {
        this.context = context;
    }

    public void process(HttpRequest request, HttpResponse response) {

        try {
            // 요청 URI에 해당하는 서블릿을 찾기 위해 매핑을 확인
            Mapper mapper = context.getMapper();
            StandardWrapper wrapper = mapper.getStandardWrapper(request.getStartLine().getRequestUri());

            if (wrapper != null) {
                wrapper.service(request, response);
            }

            // 세션이 실제 생성된 경우에만 Set-Cookie 내려줌
            HttpSession session = request.getSession(false);
            if (session != null && request.isNewSession()) {
                response.addHeader("Set-Cookie", "JSESSIONID=" + session.getId() + "; Path=/; HttpOnly");
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("Internal Server Error".getBytes());
        }
    }
    }
