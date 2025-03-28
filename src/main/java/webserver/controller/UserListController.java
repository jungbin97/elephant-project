package webserver.controller;

import db.DataBase;
import model.User;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;
import webserver.http11.session.HttpSession;

import java.util.Collection;

public class UserListController implements Controller {

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        if (isLogined(request.getSession())) {
            response.setStatusCode(200);
            response.setHeader("Content-Type", "text/html");
            response.setBody(createHtml().getBytes());
        } else {
            response.sendRedirect("/user/login.html");
        }
    }

    private boolean isLogined(HttpSession session) {
        Object user = session.getAttribute("user");

        if (user == null) {
            return false;
        }
        return true;
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
