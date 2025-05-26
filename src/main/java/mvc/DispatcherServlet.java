package mvc;

import mvc.controller.Controller;
import webserver.container.ServletContextAware;
import webserver.container.StandardContext;
import webserver.container.StandardWrapper;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;
import webserver.servlet.HttpServlet;

import java.io.IOException;

public class DispatcherServlet extends HttpServlet implements ServletContextAware {
    private final HandlerMapping handlerMapping = new HandlerMapping();
    private StandardContext context;

    @Override
    public void setServletContext(StandardContext context) {
        this.context = context;
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        Controller controller = handlerMapping.getController(request.getStartLine().getRequestUri());

        if (controller != null) {
            controller.service(request, response);
        } else {
            // DefaultServlet을 사용하여 정적 리소스 처리
            try {
                // DefaultServlet은 현재 Stadardcontext 관리 하에 있음.
                StandardWrapper wrapper = context.getMapper().getStandardWrapper("/default");
                if (wrapper != null) {
                    wrapper.service(request, response);
                } else {
                    response.setStatusCode(404);
                    response.setHeader("Content-Type", "text/plain");
                    response.setBody("Not Found".getBytes());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
