package webserver.http11;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;
import webserver.http11.request.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code HttpRequestParser}는 HTTP 요청(InputStream)을 분석하여 {@link HttpRequest} 객체로 변환하는 유틸리티 클래스입니다.
 *
 * <p>주요 파싱 대상:</p>
 * <ul>
 *     <li>요청 시작줄 (HTTP Method, URI, Version)</li>
 *     <li>요청 헤더</li>
 *     <li>요청 바디</li>
 *     <li>쿼리 파라미터 (GET, POST 공통)</li>
 * </ul>
 *
 * <h2>동작 방식</h2>
 * <ol>
 *     <li>입력 스트림에서 요청 시작 줄을 읽고, {@link HttpRequestStartLine} 객체를 생성합니다.</li>
 *     <li>이후 헤더 라인을 순차적으로 읽어 {@link HttpRequestHeader}로 구성합니다.</li>
 *     <li>헤더에 {@code Content-Length}가 있는 경우, 바디를 해당 길이만큼 읽습니다.</li>
 *     <li>헤더의 {@code Content-Type}이 {@code application/x-www-form-urlencoded}일 경우, 바디 내용을 파싱하여 쿼리 파라미터로 추가합니다.</li>
 *     <li>파싱 결과를 바탕으로 최종적으로 {@link HttpRequest} 객체를 생성하여 반환합니다.</li>
 * </ol>
 *
 * <h3>예외 처리</h3>
 * 다음과 같은 경우 {@link IOException}을 발생시킵니다:
 * <ul>
 *     <li>요청 라인이 null이거나 비어있는 경우</li>
 *     <li>헤더 형식이 잘못된 경우 (콜론 누락, 이름에 공백 포함 등)</li>
 * </ul>
 *
 * @see HttpRequest
 * @see HttpRequestStartLine
 * @see HttpRequestHeader
 * @see HttpRequestBody
 * @author jungbin97
 */
public class HttpRequestParser {
    private static final Logger log = LoggerFactory.getLogger(HttpRequestParser.class);
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    private HttpRequestParser() {
        throw new IllegalStateException("Utility class");
    }

    public static HttpRequest parse(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String reqeustStartLine = reader.readLine();

        if (reqeustStartLine == null || reqeustStartLine.isEmpty()) {
            throw new IOException("Invalid HTTP request");
        }

        String[] requestStartLineTokens = reqeustStartLine.split(" ");
        String method = requestStartLineTokens[0];
        String requestUri = requestStartLineTokens[1];
        String httpVersion = requestStartLineTokens[2];

        Map<String, String> queryParameters = new HashMap<>();
        int queryIndex = requestUri.indexOf("?");
        if (queryIndex != -1) {
            String queryString = requestUri.substring(queryIndex + 1);
            queryParameters.putAll(HttpRequestUtils.parseQueryString(queryString));
        }

        HttpRequestStartLine httpRequestStartLine = new HttpRequestStartLine(method, requestUri, httpVersion);


        Map<String, String> headers = new HashMap<>();
        String headerLine;
        while ((headerLine = reader.readLine()) != null) {
            if (headerLine.isEmpty()) {
                break;
            }
            int colonIndex = headerLine.indexOf(":");
            if (colonIndex == -1 || colonIndex == 0 || colonIndex == headerLine.length() - 1) {
                throw new IOException("Invalid HTTP header");
            }

            String headerName = headerLine.substring(0, colonIndex).trim();
            String headerValue = headerLine.substring(colonIndex + 1).trim();

            if (headerName.contains(" ")) {
                throw new IOException("Invalid HTTP header");
            }

            headers.put(headerName, headerValue);
        }
        HttpRequestHeader httpRequestHeader = new HttpRequestHeader(headers);

        String body = null;
        if (headers.containsKey(CONTENT_LENGTH)) {
            String contentLengthValue = headers.get(CONTENT_LENGTH);
            int contentLength = (contentLengthValue != null) ? Integer.parseInt(contentLengthValue) : 0;
            if (contentLength > 0) {
                body = IOUtils.readData(reader, contentLength);
            }
        }

        // Content-Type이 application/x-www-form-urlencoded인 경우에만 body를 파싱
        if (headers.containsKey(CONTENT_TYPE) && headers.get(CONTENT_TYPE).equals(X_WWW_FORM_URLENCODED)) {
            queryParameters.putAll(HttpRequestUtils.parseQueryString(body));
        }

        HttpRequestBody httpRequestBody = new HttpRequestBody(body);

        log.debug("Request : {}", new HttpRequest(httpRequestStartLine, httpRequestHeader, httpRequestBody, queryParameters));
        return new HttpRequest(httpRequestStartLine, httpRequestHeader, httpRequestBody, queryParameters);
    }
}