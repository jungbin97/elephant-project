package webserver.processor;

import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class StaticResourceProcessor {
    private static final String STATIC_RESOURCE_PATH = "./webapp";

    public HttpResponse process(HttpRequest request) throws IOException {
        String path = request.getStartLine().getRequestUri();
        String fullPath = STATIC_RESOURCE_PATH + (path.startsWith("/") ? path : "/" + path);

        if (Files.exists(Paths.get(fullPath))) {
            byte[] fileBytes = Files.readAllBytes(Paths.get(fullPath));
            String mimeType = getMimeType(path);

            return new HttpResponse(200, mimeType, fileBytes);
        }

        return new HttpResponse(404, "text/plain", "Not Found".getBytes());
    }

    public static String getMimeType(String requestUri) {
        if (requestUri.endsWith(".html")) return "text/html";
        if (requestUri.endsWith(".css")) return "text/css";
        if (requestUri.endsWith(".js")) return "application/javascript";
        if (requestUri.endsWith(".png")) return "image/png";
        return "text/plain";
    }
}
