package webserver.http11;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import webserver.http11.request.HttpRequest;
import webserver.http11.request.HttpRequestBody;
import webserver.http11.request.HttpRequestHeader;
import webserver.http11.request.HttpRequestStartLine;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 논블로킹(Non-blocking) 방식으로 들어오는 HTTP 요청을 파싱하는 상태 기반(stateful) 파서입니다.
 * <p>
 * 이 클래스는 요청 데이터가 여러 개의 {@link ByteBuffer} 조각으로 나뉘어 도착하는 NIO 환경의
 * 특성을 처리하기 위해, 내부적으로 파싱의 진행 상태를 계속 유지합니다.
 * {@link webserver.connector.nio.Http11NioProcessor}에 의해 반복적으로 호출되며,
 * 완전한 HTTP 요청 하나가 파싱될 때까지 상태를 축적합니다.
 *
 * <h2>내부 상태 머신</h2>
 * <ol>
 * <li>{@code START_LINE}: 요청의 시작 줄을 파싱합니다.</li>
 * <li>{@code HEADERS}: 헤더 부분을 파싱합니다.</li>
 * <li>{@code BODY}: Content-Length 만큼의 본문을 파싱합니다.</li>
 * <li>{@code COMPLETE}: 하나의 완전한 요청 파싱이 완료된 상태입니다.</li>
 * </ol>
 *
 * @author jungbin97
 * @see webserver.connector.nio.Http11NioProcessor
 */
public class NioHttpRequestParser {
    private static final Logger log = LoggerFactory.getLogger(NioHttpRequestParser.class);
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    private enum State {
        START_LINE,
        HEADERS,
        BODY,
        COMPLETE
    }
    private State state = State.START_LINE;

    private final StringBuilder currentLine = new StringBuilder();
    private final List<String> headerLines = new ArrayList<>();
    private String requestLine;
    private int contentLength = 0;
    private int bodyRead = 0;
    private final StringBuilder bodyBuilder = new StringBuilder();

    /**
     * 주어진 {@link ByteBuffer}로부터 데이터를 읽어 파싱을 진행합니다.
     * <p>
     * 완전한 HTTP 요청 하나가 완성되면 {@link HttpRequest} 객체를 반환합니다.
     * 아직 요청이 완성되지 않았다면 {@code null}을 반환하여 더 많은 데이터가 필요함을 알립니다.
     * 메서드 호출 후, 버퍼는 {@link ByteBuffer#compact()}를 통해 다음 읽기를 위해 준비됩니다.
     *
     * @param buffer 소켓 채널에서 읽어온 데이터가 담긴 ByteBuffer
     * @return 파싱이 완료된 {@code HttpRequest} 객체, 또는 아직 미완성일 경우 {@code null}
     * @throws IOException 요청 형식이 잘못된 경우
     */
    public HttpRequest parse(ByteBuffer buffer) throws IOException {
        // buffer 8kb
        buffer.flip();
        while (buffer.hasRemaining()) {
            byte b = buffer.get(); // 버퍼에서 바이트 읽기
            char c = (char) (b & 0xFF); // 바이트를 char로 변환
            currentLine.append(c);

            if (state == State.START_LINE || state == State.HEADERS) {
                // 현재 줄이 CRLF로 끝나는지 확인
                if (currentLine.length() >= 2 && currentLine.charAt(currentLine.length() - 2) == '\r' && currentLine.charAt(currentLine.length() - 1) == '\n') {
                    String line = currentLine.substring(0, currentLine.length() - 2);
                    currentLine.setLength(0); // 현재 줄 초기화

                    if (state == State.START_LINE) {
                        requestLine = line;
                        state = State.HEADERS;
                    } else {
                        if (line.isEmpty()) { // header 끝 도달
                            extractContentLength();
                            if (contentLength > 0) {
                                state = State.BODY; // body 읽기 상태로 전환
                            } else {
                                state = State.COMPLETE; // body가 없음, 요청 완료
                            }
                        } else {
                            headerLines.add(line);
                        }
                    }
                }
            } else if (state == State.BODY) {
                bodyBuilder.append(c);
                bodyRead++;
                if (bodyRead == contentLength) {
                    state = State.COMPLETE; // body 읽기 완료
                }
            }
        }
        buffer.compact();  // 읽기 모드 에서 쓰기 모드로 전환, 위치 남은 바이트로 설정

        if (state == State.COMPLETE) {
            return buildRequest();
        }

        return null;
    }

    /**
     * 파싱된 헤더 목록에서 Content-Length 값을 추출하여 필드에 저장합니다.
     */
    private void extractContentLength() {
        for (String header : headerLines) {
            if (header.toLowerCase().startsWith(CONTENT_LENGTH.toLowerCase() + ":")) {
                String value = header.split(":", 2)[1].trim();
                contentLength = Integer.parseInt(value);
            }
        }
    }

    /**
     * 파싱이 완료된 후, 지금까지 수집된 상태 정보들로 {@link HttpRequest} 객체를 조립합니다.
     *
     * @return 완성된 {@code HttpRequest} 객체
     * @throws IOException 요청 시작 줄 형식이 잘못된 경우
     */
    private HttpRequest buildRequest() throws IOException {
        String[] tokens = requestLine.split(" ");
        if (tokens.length != 3) throw new IOException("Invalid request start line");

        HttpRequestStartLine startLine = new HttpRequestStartLine(tokens[0], tokens[1], tokens[2]);

        Map<String, String> headersMap = new HashMap<>();
        for (String header : headerLines) {
            int colon = header.indexOf(":");
            headersMap.put(header.substring(0, colon).trim(), header.substring(colon + 1).trim());
        }
        HttpRequestHeader headers = new HttpRequestHeader(headersMap);

        // Query 파라미터 병합
        Map<String, String> queryParameters = new HashMap<>();
        int idx = startLine.getRequestUri().indexOf('?');
        if (idx != -1) {
            String query = startLine.getRequestUri().substring(idx + 1);
            queryParameters.putAll(HttpRequestUtils.parseQueryString(query));
        }

        // Body 파라미터 병합 (application/x-www-form-urlencoded 일 경우)
        String body = bodyBuilder.toString();
        if (headersMap.containsKey(CONTENT_TYPE)
                && headersMap.get(CONTENT_TYPE).equalsIgnoreCase(X_WWW_FORM_URLENCODED)) {
            queryParameters.putAll(HttpRequestUtils.parseQueryString(body));
        }

        HttpRequestBody requestBody = new HttpRequestBody(body);

        log.info("Parsed request: {}", startLine);
        return new HttpRequest(startLine, headers, requestBody, queryParameters);
    }
}
