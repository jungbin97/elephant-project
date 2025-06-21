package webserver.http11.response;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 응답을 생성하고 표현하는 클래스입니다.
 * <p>
 * 이 클래스는 응답 본문을 두가지 방시긍로 처리할 수 있도록 설계되었습니다:
 * <ol>
 * <li><b>메모리 기반 본문:</b> {@code byte[]} 배열을 사용하여 동적으로 생성된 콘텐츠(예: JSON, HTML 문자열)를 처리합니다.</li>
 * <li><b>파일 기반 본문:</b> {@link Path} 객체를 사용하여 디스크에 있는 정적 파일(예: HTML, CSS, 이미지)을 참조합니다.
 * 이 방식은 하위 I/O 계층에서 Zero-Copy 최적화를 가능하게 합니다.</li>
 * </ol>
 * 응답 본문은 두 방식 중 하나만 가질 수 있으며, 한쪽을 설정하면 다른 쪽은 초기화됩니다.
 *
 * @author jungbin97
 * @see webserver.http11.request.HttpRequest
 * @see ResponseSender
 */
public class HttpResponse {
    private int statusCode;
    private final Map<String, String> headers = new HashMap<>();
    private byte[] body;
    private Path fileBody;

    /**
     * HTTP 응답 상태 코드를 설정합니다.
     * @param statusCode 설정할 HTTP 상태 코드
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * HTTP 응답 헤더를 설정하거나 덮어씁니다.
     * @param key   헤더의 이름
     * @param value 헤더의 값
     */
    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    /**
     * 동적으로 생성된 컨텐츠 등 메모리상의 바이트 배열을 응답 본문으로 설정합니다.
     * 이 메서드가 호출되면, 기존에 설정된 파일 본문(fileBody) 참조는 null로 초기화됩니다.
     * @param body 응답 본문을 구성하는 바이트 배열
     */
    public void setBody(byte[] body) {
        this.fileBody = null;
        this.body = body;
    }

    /**
     * 정적 파일과 같이 디스크에 존재하는 파일을 응답 본문으로 설정합니다.
     * 이 메서드는 Zero-Copy 전송을 위해 사용됩니다.
     * 호출 시, 기존에 설정된 메모리 본문(body) 참조는 null로 초기화됩니다.
     * @param path 전송할 파일의 Path 객체
     */
    public void setFileBody(Path path) {
        this.body = null;
        this.fileBody = path;
    }

    /**
     * 파일 기반 응답 본문의 {@link Path}를 반환합니다.
     * @return 파일 본문의 Path 객체. 설정되지 않았다면 {@code null}.
     */
    public Path getFileBody() {
        return fileBody;
    }

    /**
     * 응답 본문이 파일(Path) 형태로 설정되었는지 여부를 확인합니다.
     * @return 파일 본문이 설정되어 있으면 true, 아니면 false
     */
    public boolean hasFileBody() {
        return fileBody != null;
    }

    /**
     * 302 Found 리다이렉트 응답을 설정하는 편의 메서드입니다.
     * @param location 리다이렉트할 URL
     */
    public void sendRedirect(String location) {
        this.statusCode = 302;
        headers.put("Location", location);
        headers.put("Content-Length", "0");
    }

    public String getStatusMessage() {
        return switch (statusCode) {
            case 200 -> "OK";
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 307 -> "Temporary Redirect";
            case 308 -> "Permanent Redirect";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Unknown Status";
        };
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }
}