package trunk.servlet;

import trunk.http11.request.HttpRequest;
import trunk.http11.response.HttpResponse;

import java.io.IOException;

public class DummyServlet extends HttpServlet {
    public static boolean destroyed = false;

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) throws IOException {
        // 테스트를 위한 더미 서블릿
    }

    @Override
    public void destroy() {
        destroyed = true;
    }
}
