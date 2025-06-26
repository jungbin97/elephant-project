package trunk.servlet;

import trunk.container.ServletContextAware;
import trunk.container.StandardContext;
import trunk.http11.request.HttpRequest;
import trunk.http11.response.HttpResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * {@code DefaultServlet}는 정적 리소스를 요청을 처리하는 서블릿입니다. <br>
 * 파일을 직접 읽어 메모리에 올리는 대신, 파일의 경로(Path)와 메타데이터만 HttpResponse에 설정하여
 * 하위 Connector 계층에서 Zero-Copy를 수행할 수 있도록 책임을 위임합니다.
 *
 * @see HttpServlet
 * @author jungbin97
 */
public class DefaultServlet extends HttpServlet implements ServletContextAware {
    private StandardContext context;

    @Override
    public void setServletContext(StandardContext context) {
        this.context = context;
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        String requestPath = request.getStartLine().getRequestUri();
        String realPathStr = context.getRealPath(requestPath);

        if (realPathStr == null) {
            sendNotFound(response);
            return;
        }

        Path filePath = Paths.get(realPathStr);
        if (!Files.isRegularFile(filePath)) {
            sendNotFound(response);
            return;
        }

        long contentLength = Files.size(filePath);
        String mimeType = getMimeType(requestPath);

        response.setStatusCode(200);
        response.setHeader("Content-Type", mimeType);
        response.setHeader("Content-Length", String.valueOf(contentLength));
        // 파일을 직접 읽지 않고, Path 객체를 응답 본문으로 설정
        response.setFileBody(filePath);
        }

    private void sendNotFound(HttpResponse response) {
        response.setStatusCode(404);
        response.setHeader("Content-Type", "text/plain; charset=utf-8");
        response.setBody("Not Found".getBytes());
    }

    public static String getMimeType(String requestUri) {
        if (requestUri.endsWith(".html")) return "text/html";
        if (requestUri.endsWith(".css")) return "text/css";
        if (requestUri.endsWith(".js")) return "application/javascript";
        if (requestUri.endsWith(".png")) return "image/png";
        if (requestUri.endsWith(".jpg") || requestUri.endsWith(".jpeg")) return "image/jpeg";
        if (requestUri.endsWith(".gif")) return "image/gif";
        if (requestUri.endsWith(".ico")) return "image/x-icon";
        return "text/plain";
    }
}
