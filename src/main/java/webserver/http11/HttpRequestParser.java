package webserver.http11;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.IOUtils;
import webserver.http11.request.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestParser {
    private static final Logger log = LoggerFactory.getLogger(HttpRequestParser.class);
    private static final String CONTENT_LENGTH = "Content-Length";

    private HttpRequestParser() {
        throw new IllegalStateException("Utility class");
    }

    public static HttpRequest parse(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String reqeustStartLine = reader.readLine();

        if (reqeustStartLine == null || reqeustStartLine.isEmpty()) {
            throw new IOException("Invalid HTTP request");
        }

        String[] requestStartLineTokens = reqeustStartLine.split(" ");
        String method = requestStartLineTokens[0];
        String requestUri = requestStartLineTokens[1];
        String httpVersion = requestStartLineTokens[2];
        HttpRequestStartLine httpRequestStartLine = new HttpRequestStartLine(method, requestUri, httpVersion);

        Map<String, String> headers = new HashMap<>();
        String headerLine;

        while ((headerLine = reader.readLine()) != null) {
            if (headerLine.isEmpty()) {
                break;
            }
            log.debug("Header Line: {}", headerLine);
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
        log.debug("Body 내용: {}", body);
        HttpRequestBody httpRequestBody = new HttpRequestBody(body);

        log.debug("Request : {}", new HttpRequest(httpRequestStartLine, httpRequestHeader, httpRequestBody));

        return new HttpRequest(httpRequestStartLine, httpRequestHeader, httpRequestBody);
    }
}