package webserver.controller;

import model.User;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;

import java.util.Map;

public class UserController implements Controller {

    @Override
    public HttpResponse handle(HttpRequest request) {
        Map<String, String> queryParameters = request.getQueryParameters();
        String userId = queryParameters.get("userId");
        String password = queryParameters.get("password");
        String name = queryParameters.get("name");
        String email = queryParameters.get("email");

        User user = new User(userId, password, name, email);

        // index.html로 redirect
        return new HttpResponse(302, "/index.html");
    }
}
