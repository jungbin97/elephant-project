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

// 논블로킹 방식으로 상태를 유지해야함. 따라서 static으로 만들지 않음.
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

    public HttpRequest parse(ByteBuffer buffer) throws IOException {
        // buffer 8kb
        buffer.flip();
        while (buffer.hasRemaining()) {
            Byte b = buffer.get(); // 버퍼에서 바이트 읽기
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

    private void extractContentLength() {
        for (String header : headerLines) {
            if (header.toLowerCase().startsWith(CONTENT_LENGTH.toLowerCase() + ":")) {
                String value = header.split(":", 2)[1].trim();
                contentLength = Integer.parseInt(value);
            }
        }
    }

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
