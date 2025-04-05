package webserver.staticresource;

import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;
import webserver.servlet.HttpServlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DefaultServlet extends HttpServlet {
    private static final String STATIC_RESOURCE_PATH = "./webapp";

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {

        String path = request.getStartLine().getRequestUri();
        String fullPath = STATIC_RESOURCE_PATH + (path.startsWith("/") ? path : "/" + path);

        if (Files.exists(Paths.get(fullPath))) {
            byte[] fileBytes = Files.readAllBytes(Paths.get(fullPath));
            String mimeType = getMimeType(path);

            response.setStatusCode(200);
            response.setHeader("Content-Type", mimeType);
            response.setBody(fileBytes);
        }
    }

    public static String getMimeType(String requestUri) {
        if (requestUri.endsWith(".html")) return "text/html";
        if (requestUri.endsWith(".css")) return "text/css";
        if (requestUri.endsWith(".js")) return "application/javascript";
        if (requestUri.endsWith(".png")) return "image/png";
        return "text/plain";
    }
}
