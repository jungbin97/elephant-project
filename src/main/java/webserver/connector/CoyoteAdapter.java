package webserver.connector;

import webserver.container.Mapper;
import webserver.container.StandardContext;
import webserver.container.StandardWrapper;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;

import java.io.IOException;

public class CoyoteAdapter {
    private final StandardContext context;

    public CoyoteAdapter(StandardContext context) {
        this.context = context;
    }

    public void service(HttpRequest request, HttpResponse response) throws IOException {
        // 요청 URI에 해당하는 서블릿을 찾기 위해 매핑을 확인
        Mapper mapper = context.getMapper();
        StandardWrapper wrapper = mapper.getStandardWrapper(request.getStartLine().getRequestUri());

        if (wrapper != null) {
            wrapper.service(request, response);
        } else {
            response.setStatusCode(404);
            response.setBody("Not Found".getBytes());
        }
    }
}
