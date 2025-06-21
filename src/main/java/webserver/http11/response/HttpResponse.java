package webserver.http11.response;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private int statusCode;
    private final Map<String, String> headers = new HashMap<>();
    private byte[] body;
    private Path fileBody;

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

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