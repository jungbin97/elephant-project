package webserver.http11;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;
import webserver.http11.request.HttpRequest;
import webserver.http11.request.HttpRequestBody;
import webserver.http11.request.HttpRequestHeader;
import webserver.http11.request.HttpRequestStartLine;

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
public class BioHttpRequestParser {
    private static final Logger log = LoggerFactory.getLogger(BioHttpRequestParser.class);
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    private BioHttpRequestParser() {
        throw new IllegalStateException("Utility class");
    }

    public static HttpRequest parse(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1));

        // Start Line 파싱
        HttpRequestStartLine startLine = parseStartLine(reader);
        if (startLine == null) {
            return null; // 빈 요청일 경우
        }

        // Header 파싱
        Map<String, String> headersMap = parseHeaders(reader);
        HttpRequestHeader headers = new HttpRequestHeader(headersMap);

        // Body 파싱
        String body = parseBody(reader, headersMap);

        // 쿼리 파라미터 파싱
        Map<String, String> queryParameters = parseQueryParameters(startLine.getRequestUri(), body, headersMap);

        HttpRequestBody requestBody = new HttpRequestBody(body);
        HttpRequest request = new HttpRequest(startLine, headers, requestBody, queryParameters);

        log.debug("Parsed request: {}", startLine);
        return request;
    }

    /**
     * HTTP 요청 시작줄을 파싱합니다.
     */
    private static HttpRequestStartLine parseStartLine(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null || line.isBlank()) {
            return null; // 요청이 없거나 비어있는 경우
        }
        String[] tokens = line.split(" ");
        if (tokens.length != 3) {
            throw new IOException("Invalid request start line: " + line);
        }
        return new HttpRequestStartLine(tokens[0], tokens[1], tokens[2]);
    }

    /**
     * HTTP 헤더들을 파싱합니다.
     */
    private static Map<String, String> parseHeaders(BufferedReader reader) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String headerLine;
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            int colonIndex = headerLine.indexOf(":");
            if (colonIndex <= 0 || colonIndex == headerLine.length() - 1) {
                throw new IOException("Invalid header format: " + headerLine);
            }

            String headerName = headerLine.substring(0, colonIndex).trim();
            String headerValue = headerLine.substring(colonIndex + 1).trim();

            if (headerName.contains(" ")) { // 헤더 이름에 공백이 있으면 유효하지 않음
                throw new IOException("Invalid HTTP header name contains space: " + headerName);
            }
            headers.put(headerName, headerValue);
        }
        return headers;
    }

    /**
     * HTTP 요청 바디를 파싱합니다. Content-Length 헤더를 기반으로 읽습니다.
     */
    private static String parseBody(BufferedReader reader, Map<String, String> headers) throws IOException {
        if (!headers.containsKey(CONTENT_LENGTH)) {
            return null;
        }

        String contentLengthValue = headers.get(CONTENT_LENGTH);
        int contentLength = 0;
        try {
            contentLength = (contentLengthValue != null) ? Integer.parseInt(contentLengthValue) : 0;
        } catch (NumberFormatException e) {
            log.warn("Invalid Content-Length header value: {}", contentLengthValue);
            return null;
        }


        if (contentLength > 0) {
            // IOUtils.readData는 BufferedReader에서 정확히 contentLength 만큼 읽어옵니다.
            return IOUtils.readData(reader, contentLength);
        }
        return null;
    }

    /**
     * 요청 URI와 바디에서 쿼리 파라미터를 파싱합니다.
     */
    private static Map<String, String> parseQueryParameters(String requestUri, String body, Map<String, String> headers) {
        Map<String, String> queryParameters = new HashMap<>();

        int queryIndex = requestUri.indexOf("?");
        if (queryIndex != -1) {
            String queryString = requestUri.substring(queryIndex + 1);
            queryParameters.putAll(HttpRequestUtils.parseQueryString(queryString));
        }

        if (body != null && X_WWW_FORM_URLENCODED.equals(headers.get(CONTENT_TYPE))) {
            queryParameters.putAll(HttpRequestUtils.parseQueryString(body));
        }

        return queryParameters;
    }
}