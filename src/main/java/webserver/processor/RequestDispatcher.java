package webserver.processor;

import webserver.controller.Controller;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;

public class RequestDispatcher {
    private final HandlerMapping handlerMapping = new HandlerMapping();

    public void dispatch(HttpRequest request, HttpResponse response) {
        Controller controller = handlerMapping.getController(request.getStartLine().getRequestUri());

        if (controller != null) {
            controller.service(request, response);
        } else {
            response.setStatusCode(404);
            response.setHeader("Content-Type", "text/plain");
            response.setBody("Not Found".getBytes());
        }
    }
}
