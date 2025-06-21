package webserver.http11.response;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link HttpResponse} 객체를 실제 출력 스트림이나 {@link ByteBuffer}로 변환하는 역할을 담당하는 유틸리티 클래스입니다.
 * <p>
 * 이 클래스는 상태를 가지지 않는(stateless) 메서드들로만 구성되어 있어 스레드에 안전합니다.
 * BIO(Blocking I/O)와 NIO(Non-blocking I/O) 방식의 응답 전송을 모두 지원합니다.
 *
 * @author jungbin97
 * @see HttpResponse
 */
public class ResponseSender {

    /**
     * 이 클래스는 인스턴스화할 수 없습니다.
     */
    private ResponseSender() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * BIO(Blocking I/O) 방식으로 HttpResponse의 내용을 주어진 {@link DataOutputStream}에 씁니다.
     * 상태 라인, 헤더, 본문을 순서대로 전송합니다.
     *
     * @param response 전송할 {@code HttpResponse} 객체
     * @param dos      데이터를 쓸 대상 출력 스트림
     * @throws IOException 스트림에 쓰는 도중 I/O 오류가 발생할 경우
     */
    public static void sendResponseBIO(HttpResponse response, DataOutputStream dos) throws IOException {
        byte[] body = response.getBody();
        // Content-Length
        int length = (body != null) ? body.length : 0;

        Map<String, String> headers = new HashMap<>(response.getHeaders());
        headers.putIfAbsent("Content-Length", String.valueOf(length));

        dos.writeBytes("HTTP/1.1 " + response.getStatusCode() + " " + response.getStatusMessage() + "\r\n");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            dos.writeBytes(entry.getKey() + ": " + entry.getValue() + "\r\n");
        }
        dos.writeBytes("\r\n");

        if (body != null && body.length > 0) {
            dos.write(body);
        }
        dos.flush();
    }

    /**
     * NIO(Non-blocking I/O) 방식으로 HttpResponse의 내용을 하나의 {@link ByteBuffer}로 변환합니다.
     * 이 메서드는 주로 메모리 기반의 동적 콘텐츠를 전송할 때 사용됩니다.
     *
     * @param response 변환할 {@code HttpResponse} 객체
     * @return 상태 라인, 헤더, 본문이 모두 포함된 단일 {@code ByteBuffer}. 버퍼는 읽기 쉽도록 flip()된 상태입니다.
     */
    public static ByteBuffer sendResponseNIO(HttpResponse response) throws IOException {
        byte[] body = response.getBody();
        int length = (body != null) ? body.length : 0;

        Map<String, String> headers = new HashMap<>(response.getHeaders());
        headers.putIfAbsent("Content-Length", String.valueOf(length));

        StringBuilder responseBuilder = new StringBuilder();

        // Status line
        responseBuilder
                .append("HTTP/1.1 ")
                .append(response.getStatusCode()).append(" ")
                .append(response.getStatusMessage())
                .append("\r\n");

        // Headers
        headers.forEach((key, value) ->
                responseBuilder.append(key).append(": ").append(value).append("\r\n"));

        responseBuilder.append("\r\n");

        byte[] headerBytes = responseBuilder.toString().getBytes(StandardCharsets.ISO_8859_1);
        int totalLength = headerBytes.length + ((body != null) ? body.length : 0);
        ByteBuffer buffer = ByteBuffer.allocateDirect(totalLength);

        buffer.put(headerBytes);

        if (body != null) {
            buffer.put(body);
        }

        buffer.flip();
        return buffer;
    }

    /**
     * HttpResponse 객체로부터 헤더 정보만으로 구성된 ByteBuffer를 생성합니다.
     * Zero-Copy 파일 전송 시 헤더를 먼저 보내기 위해 사용됩니다.
     *
     * @param response HTTP 응답 객체
     * @return 헤더 정보가 담긴 ByteBuffer. 버퍼는 읽기 쉽도록 flip()된 상태입니다.
     */
    public static ByteBuffer createHeaderBuffer(HttpResponse response) {
        StringBuilder headerBuilder = new StringBuilder();

        // Status line
        headerBuilder
                .append("HTTP/1.1 ")
                .append(response.getStatusCode()).append(" ")
                .append(response.getStatusMessage())
                .append("\r\n");

        // Headers
        response.getHeaders().forEach((key, value) ->
                headerBuilder.append(key).append(": ").append(value).append("\r\n"));

        headerBuilder.append("\r\n");

        return ByteBuffer.wrap(headerBuilder.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

}
