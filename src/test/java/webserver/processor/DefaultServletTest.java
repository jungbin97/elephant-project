package webserver.processor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import webserver.http11.request.HttpRequest;
import webserver.http11.request.HttpRequestStartLine;
import webserver.http11.response.HttpResponse;
import webserver.staticresource.DefaultServlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultServletTest {
    private final DefaultServlet defaultServlet = new DefaultServlet();

    @Test
    @DisplayName("정적 리소스가 존재 하는 경우 200 응답을 반환해야 한다.")
    void testService_200() throws IOException {
        // given
        HttpRequest mockRequest = mock(HttpRequest.class);
        HttpRequestStartLine mockStartLine = mock(HttpRequestStartLine.class);

        when(mockRequest.getStartLine()).thenReturn(mockStartLine);
        when(mockRequest.getStartLine().getRequestUri()).thenReturn("/test.html");

        // 임시 파일 생성
        String testFilePath = "./webapp/test.html";
        Files.write(Paths.get(testFilePath), "<html>test</html>".getBytes());

        HttpResponse response = new HttpResponse();
        // when
        defaultServlet.service(mockRequest, response);

        // then
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getHeader("Content-Type")).isEqualTo("text/html");
        assertThat(response.getBody()).isEqualTo("<html>test</html>".getBytes());

        // 테스트 후 파일 삭제
        Files.deleteIfExists(Paths.get(testFilePath));
    }
}
