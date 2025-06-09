package webserver.http11;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import webserver.http11.request.HttpRequest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NioHttpRequestParserTest {

    @Test
    @DisplayName("완전한 GET 요청을 한번에 넘기면 정상적으로 파싱해야 한다.")
    void parseGetRequest() throws IOException {
        // given
        String rawRequest =
                "GET /index.html HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "\r\n";

        ByteBuffer buffer = ByteBuffer.allocate(8192);
        buffer.put(rawRequest.getBytes(StandardCharsets.ISO_8859_1));
        NioHttpRequestParser parser = new NioHttpRequestParser();

        // when
        HttpRequest httpRequest = parser.parse(buffer);


        // then
        assertThat(httpRequest.getStartLine().getMethod()).isEqualTo("GET");
        assertThat(httpRequest.getStartLine().getRequestUri()).isEqualTo("/index.html");
        assertThat(httpRequest.getStartLine().getHttpVersion()).isEqualTo("HTTP/1.1");
        assertThat(httpRequest.getHeaders().getHeaders()).containsEntry("Host", "localhost:8080");
    }

    @Test
    @DisplayName("완전한 POST 요청을 한번에 넘기면 정상적으로 파싱해야 한다.")
    void parsePostRequest() throws Exception {
        // given
        String rawRequest =
                "POST /index.html HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Content-Length: 10\r\n" +
                "\r\n" +
                "1234567890여기부터는 길이초과";
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        buffer.put(rawRequest.getBytes(StandardCharsets.ISO_8859_1));
        NioHttpRequestParser parser = new NioHttpRequestParser();

        // when
        HttpRequest httpRequest = parser.parse(buffer);

        // then
        assertThat(httpRequest.getStartLine().getMethod()).isEqualTo("POST");
        assertThat(httpRequest.getStartLine().getRequestUri()).isEqualTo("/index.html");
        assertThat(httpRequest.getStartLine().getHttpVersion()).isEqualTo("HTTP/1.1");
        assertThat(httpRequest.getHeaders().getHeaders()).containsEntry("Host", "localhost:8080");
        assertThat(httpRequest.getHeaders().getHeaders()).containsEntry("Content-Length", "10");
        assertThat(httpRequest.getBody().getBody()).isEqualTo("1234567890");
    }

    @Test
    @DisplayName("POST 요청을 두 번에 나눠서 들어와도 상태를 유지해 파싱해야 한다.")
    void parseSplitPost() throws Exception {
        // given
        String rawRequest =
                "POST /login HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "Content-Length: 13\r\n" +
                "\r\n" +
                "id=alice&pw=1";

        // when & then
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        NioHttpRequestParser parser = new NioHttpRequestParser();

        // 1차 요청: 헤더만 전송
        int split = rawRequest.indexOf("\r\n\r\n") + 4;     // 헤더 끝 위치
        buffer.put(rawRequest.substring(0, split).getBytes(StandardCharsets.ISO_8859_1));

        assertThat(parser.parse(buffer)).isNull(); // 아직 요청이 완성되지 않음

        // 2차 요청: 바디 전송
        buffer.put(rawRequest.substring(split).getBytes(StandardCharsets.ISO_8859_1));
        HttpRequest httpRequest = parser.parse(buffer);

        assertThat(httpRequest).isNotNull();
        assertThat(httpRequest.getStartLine().getMethod()).isEqualTo("POST");
        assertThat(httpRequest.getStartLine().getRequestUri()).isEqualTo("/login");
        assertThat(httpRequest.getBody().getBody()).isEqualTo("id=alice&pw=1");
        assertThat(httpRequest.getQueryParameters())
                .containsEntry("id", "alice")
                .containsEntry("pw", "1");
    }

    @Test
    @DisplayName("잘못된 Start Line이 들어오면 예외를 발생시킨다.")
    void invalidStartLine() {
        // given
        String rawRequest =
                "BADSTARTLINE\r\n" +
                "Host: localhost:8080\r\n" +
                "\r\n";

        ByteBuffer buffer = ByteBuffer.allocate(8192);
        buffer.put(rawRequest.getBytes(StandardCharsets.ISO_8859_1));

        // when
        NioHttpRequestParser parser = new NioHttpRequestParser();

        // then
        assertThatThrownBy(() -> parser.parse(buffer))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Invalid request start line");
    }
}