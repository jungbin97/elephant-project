package webserver.controller;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;

import java.util.Collection;

public class UserListController implements Controller {

    @Override
    public HttpResponse handle(HttpRequest request) {
        if (isLogined(request)) {
            return new HttpResponse(200, "text/html", createHtml().getBytes());
        }

        return new HttpResponse(302, "/user/login.html");
    }

    private boolean isLogined(HttpRequest request) {
        String cookie = request.getHeaders().getHeaders().get("Cookie");
        if (cookie == null) {
            return false;
        }
        String value = HttpRequestUtils.parseCookies(cookie).get("logined");
        return Boolean.parseBoolean(value);
    }

    private String createHtml() {
        Collection<User> users = DataBase.findAll();
        StringBuilder sb = new StringBuilder("<html><body>");
        sb.append("<h1>User List</h1>");
        sb.append("<table border='1'>");
        sb.append("<tr><th>ID</th><th>Name</th><th>Email</th></tr>");

        for (User user : users) {
            sb.append("<tr>");
            sb.append("<td>").append(user.getUserId()).append("</td>");
            sb.append("<td>").append(user.getName()).append("</td>");
            sb.append("<td>").append(user.getEmail()).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        sb.append("</body></html>");

        return sb.toString();
    }
}
