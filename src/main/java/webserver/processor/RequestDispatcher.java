package webserver.processor;

import webserver.controller.Controller;
import webserver.controller.LoginController;
import webserver.controller.UserController;
import webserver.controller.UserListController;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RequestDispatcher {
    private final StaticResourceProcessor staticResourceProcessor;
    private final Map<String, Controller> controllers;

    public RequestDispatcher(StaticResourceProcessor staticResourceProcessor) {
        this.staticResourceProcessor = staticResourceProcessor;
        this.controllers = new HashMap<>();

        // Add controllers
        controllers.put("/user/create", new UserController());
        controllers.put("/user/login", new LoginController());
        controllers.put("/user/list", new UserListController());
    }

    public HttpResponse dispatch(HttpRequest request) throws IOException {
        String requestUri = request.getStartLine().getRequestUri();

        // Dispatch to controller
        for (Map.Entry<String, Controller> entry : controllers.entrySet()) {
            if (requestUri.equals(entry.getKey())) {
                return entry.getValue().handle(request);
            }
        }

        return staticResourceProcessor.process(request);
    }
}
