package webserver.http11;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import webserver.http11.request.HttpRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class HttpRequestParserTest {
    @Test
    @DisplayName("GET 요청 결과를 정상적으로 파싱해야 한다.")
    void parseGetRequest() throws IOException {
        // given
        String rawRequest = "GET /index.html HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "\r\n";

        ByteArrayInputStream inputstream = new ByteArrayInputStream(rawRequest.getBytes());

        // when
        HttpRequest httpRequest = HttpRequestParser.parse(inputstream);

        // then
        assertThat(httpRequest.getStartLine().getMethod()).isEqualTo("GET");
        assertThat(httpRequest.getStartLine().getRequestUri()).isEqualTo("/index.html");
        assertThat(httpRequest.getStartLine().getHttpVersion()).isEqualTo("HTTP/1.1");
        assertThat(httpRequest.getHeaders().getHeader("Host")).isEqualTo("localhost:8080");
    }

    @Test
    @DisplayName("잘못된 요청 라인을 파싱하면 예외가 발생해야 한다.")
    void parseInvalidRequestLine() {
        // given
        String rawRequest = "\r\n";
        ByteArrayInputStream inputstream = new ByteArrayInputStream(rawRequest.getBytes());

        // then
        assertThatThrownBy(() -> HttpRequestParser.parse(inputstream))
                .isInstanceOf(IOException.class)
                .hasMessage("Invalid HTTP request");
    }

    @Test
    @DisplayName("잘못된 헤더를 파싱하면 예외가 발생해야 한다.")
    void parseInvalidHeader() {
        // given
        String rawRequest = "GET /index.html HTTP/1.1\r\n" +
                "Host localhost:8080\r\n" +
                "\r\n";
        ByteArrayInputStream inputstream = new ByteArrayInputStream(rawRequest.getBytes());

        // then
        assertThatThrownBy(() -> HttpRequestParser.parse(inputstream))
                .isInstanceOf(IOException.class)
                .hasMessage("Invalid HTTP header");
    }

    @Test
    @DisplayName("POST 요청 결과를 정상적으로 파싱해야 한다.")
    void parsePostRequest() throws Exception {
        // given
        String rawRequest = "POST /index.html HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Content-Length: 10\r\n" +
                "\r\n" +
                "1234567890여기부터는 길이초과";

        ByteArrayInputStream inputstream = new ByteArrayInputStream(rawRequest.getBytes());

        // when
        HttpRequest httpRequest = HttpRequestParser.parse(inputstream);

        // then
        assertThat(httpRequest.getStartLine().getMethod()).isEqualTo("POST");
        assertThat(httpRequest.getStartLine().getRequestUri()).isEqualTo("/index.html");
        assertThat(httpRequest.getStartLine().getHttpVersion()).isEqualTo("HTTP/1.1");
        assertThat(httpRequest.getHeaders().getHeader("Host")).isEqualTo("localhost:8080");
        assertThat(httpRequest.getHeaders().getHeader("Content-Length")).isEqualTo("10");
        assertThat(httpRequest.getBody().getBody()).isEqualTo("1234567890");
    }

    @Test
    @DisplayName("바디가 없는 요청에도 정상적으로 파싱해야 한다.")
    void parseRequestWithoutBody() throws IOException {
        // given
        String rawRequest = "DELETE /index.html HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "\r\n";

        ByteArrayInputStream inputstream = new ByteArrayInputStream(rawRequest.getBytes());

        // when
        HttpRequest httpRequest = HttpRequestParser.parse(inputstream);

        // then
        assertThat(httpRequest.getStartLine().getMethod()).isEqualTo("DELETE");
        assertThat(httpRequest.getStartLine().getRequestUri()).isEqualTo("/index.html");
        assertThat(httpRequest.getStartLine().getHttpVersion()).isEqualTo("HTTP/1.1");
        assertThat(httpRequest.getHeaders().getHeader("Host")).isEqualTo("localhost:8080");
    }

    @Test
    @DisplayName("GET 요청의 쿼리 스트링을 정상적으로 파싱해야 한다.")
    void parseGetQueryString() throws IOException {
        // given
        String rawRequest = "GET /index.html?name=abc&age=20 HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "\r\n";

        ByteArrayInputStream inputstream = new ByteArrayInputStream(rawRequest.getBytes());

        // when
        HttpRequest httpRequest = HttpRequestParser.parse(inputstream);

        // then
        assertThat(httpRequest.getStartLine().getMethod()).isEqualTo("GET");
        assertThat(httpRequest.getStartLine().getRequestUri()).isEqualTo("/index.html");
        assertThat(httpRequest.getStartLine().getHttpVersion()).isEqualTo("HTTP/1.1");
        assertThat(httpRequest.getStartLine().getQueryParams()).containsEntry("name", "abc");
        assertThat(httpRequest.getStartLine().getQueryParams()).containsEntry("age", "20");
    }

}
