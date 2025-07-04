package mvc.controller;

import mvc.db.DataBase;
import mvc.model.User;
import trunk.http11.request.HttpRequest;
import trunk.http11.response.HttpResponse;
import trunk.http11.session.HttpSession;

import java.util.Map;

public class LoginController implements Controller {

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        Map<String, String> queryParameters = request.getQueryParameters();
        String userId = queryParameters.get("userId");
        String password = queryParameters.get("password");

        User user = DataBase.findUserById(userId);

        if (user != null && user.getPassword().equals(password)) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            response.sendRedirect("/index.html");
        } else {
            response.sendRedirect("/user/login_failed.html");
        }
    }
}