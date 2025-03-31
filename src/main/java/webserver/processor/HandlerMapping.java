package webserver.processor;

import webserver.controller.Controller;
import webserver.controller.LoginController;
import webserver.controller.UserController;
import webserver.controller.UserListController;

import java.util.HashMap;
import java.util.Map;

public class HandlerMapping {
    private final Map<String, Controller> controllers = new HashMap<>();


    public HandlerMapping() {
        // Add controllers
        controllers.put("/user/create", new UserController());
        controllers.put("/user/login", new LoginController());
        controllers.put("/user/list", new UserListController());
    }

    public Controller getController(String requestUri) {
        return controllers.get(requestUri);
    }
}
