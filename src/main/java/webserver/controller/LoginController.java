package webserver.controller;

import db.DataBase;
import model.User;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;

import java.util.Map;

public class LoginController implements Controller {

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        Map<String, String> queryParameters = request.getQueryParameters();
        String userId = queryParameters.get("userId");
        String password = queryParameters.get("password");

        User user = DataBase.findUserById(userId);

        if (user != null && user.getPassword().equals(password)) {
            response.addHeader("Set-Cookie", "logined=true");
            response.sendRedirect("/index.html");
        } else {
            response.addHeader("Set-Cookie", "logined=false");
            response.sendRedirect("/user/login_failed.html");
        }
    }
}