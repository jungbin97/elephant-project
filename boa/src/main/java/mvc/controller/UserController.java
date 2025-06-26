package mvc.controller;

import mvc.db.DataBase;
import mvc.model.User;
import trunk.http11.request.HttpRequest;
import trunk.http11.response.HttpResponse;

import java.util.Map;

public class UserController implements Controller {

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        Map<String, String> queryParameters = request.getQueryParameters();
        String userId = queryParameters.get("userId");
        String password = queryParameters.get("password");
        String name = queryParameters.get("name");
        String email = queryParameters.get("email");

        User user = new User(userId, password, name, email);
        DataBase.addUser(user);

        response.sendRedirect("/index.html");
    }
}
