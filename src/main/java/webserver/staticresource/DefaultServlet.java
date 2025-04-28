package webserver.staticresource;

import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;
import webserver.servlet.HttpServlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * {@code DefaultServlet}는 정적 리소스를 요청을 처리하는 서블릿입니다.
 *
 * <p>
 * HTML, CSS, JavaScript, 이미지 파일 등 정적 자원을 요청 경로에 따라 읽어 HTTP 응답으로 반환합니다. <br>
 * Tomcat의 기본 정적 파일 서블릿 역할과 유사한 책임을 수행합니다.
 * </p>
 *
 * <h2>기능</h2>
 * <ul>
 *     <li>요청 URI에 해당하는 파일을 {@code ./webapp} 경로 하위에서 탐색</li>
 *     <li>존재하는 경우, MIME 타입을 설정하고 바디에 파일 데이터를 담아 응답</li>
 *     <li>지원하지 않는 확장자에 대해서는 {@code text/plain} MIME 타입으로 응답</li>
 * </ul>
 *
 * <h2>예시</h2>
 * <pre>{@code
 * GET /index.html HTTP/1.1
 *
 * → ./webapp/index.html 파일을 찾아 Content-Type: text/html 과 함께 응답
 * }</pre>
 *
 * @see HttpServlet
 * @see HttpRequest
 * @see HttpResponse
 * @author jungbin97
 */
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
