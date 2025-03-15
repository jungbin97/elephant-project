package webserver.controller;

import db.DataBase;
import model.User;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;

import java.util.Map;

public class LoginController implements Controller {

    @Override
    public HttpResponse handle(HttpRequest request) {
        Map<String, String> queryParameters = request.getQueryParameters();
        String userId = queryParameters.get("userId");
        String password = queryParameters.get("password");

        User user = DataBase.findUserById(userId);

        HttpResponse response;
        if (user != null && user.getPassword().equals(password)) {
            response = new HttpResponse(302, "/index.html");
            response.addHeader("Set-Cookie", "logined=true");
        } else {
            response = new HttpResponse(302, "/user/login_failed.html");
            response.addHeader("Set-Cookie", "logined=false");
        }
        return response;
    }
}