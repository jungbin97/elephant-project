package webserver.processor;

import webserver.controller.Controller;
import webserver.controller.UserController;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RequestDispatcher {
    private final StaticResourceProcessor staticResourceProcessor;
    private final Map<String, Controller> controllers;

    public RequestDispatcher(StaticResourceProcessor staticResourceHandler) {
        this.staticResourceProcessor = staticResourceHandler;
        this.controllers = new HashMap<>();

        // Add controllers
        controllers.put("/user/create", new UserController());
    }

    public HttpResponse dispatch(HttpRequest request) throws IOException {
        String requestUri = request.getStartLine().getRequestUri();

        // Dispatch to controller
        for (Map.Entry<String, Controller> entry : controllers.entrySet()) {
            if (requestUri.startsWith(entry.getKey())) {
                return entry.getValue().handle(request);
            }
        }

        return staticResourceProcessor.process(request);
    }
}
