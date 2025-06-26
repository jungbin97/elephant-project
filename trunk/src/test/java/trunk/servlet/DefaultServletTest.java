package trunk.servlet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import trunk.http11.request.HttpRequest;
import trunk.http11.request.HttpRequestStartLine;
import trunk.http11.response.HttpResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultServletTest {
    private final DefaultServlet defaultServlet = new DefaultServlet();
    private static final String TEST_DIR = "./webapp";
    private static final String TEST_FILE_NAME = "/test.html";
    private static final Path TEST_FILE_PATH = Paths.get(TEST_DIR + TEST_FILE_NAME);
    private static final String TEST_CONTENT = "<html>test</html>";

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(TEST_FILE_PATH.getParent());
        Files.write(TEST_FILE_PATH, TEST_CONTENT.getBytes());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(TEST_FILE_PATH);
    }

    @Test
    @DisplayName("정적 리소스가 존재 하는 경우, 응답 객체에 파일 메타데이터를 정확히 설정해야 한다.")
    void testService_200() throws IOException {
        // given
        HttpRequest mockRequest = mock(HttpRequest.class);
        HttpRequestStartLine mockStartLine = mock(HttpRequestStartLine.class);

        when(mockRequest.getStartLine()).thenReturn(mockStartLine);
        when(mockRequest.getStartLine().getRequestUri()).thenReturn(TEST_FILE_NAME);

        HttpResponse response = new HttpResponse();

        // when
        defaultServlet.service(mockRequest, response);

        // then
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getHeader("Content-Type")).isEqualTo("text/html");
        assertThat(response.getHeader("Content-Length")).isEqualTo(String.valueOf(TEST_CONTENT.length()));
        assertThat(response.hasFileBody()).isTrue();
        assertThat(response.getFileBody()).isEqualTo(TEST_FILE_PATH.toAbsolutePath().normalize());
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("정적 리소스가 존재하지 않는 경우 404 응답을 반환해야한다.")
    void testService_404() throws Exception {
        // given
        HttpRequest mockRequest = mock(HttpRequest.class);
        HttpRequestStartLine mockStartLine = mock(HttpRequestStartLine.class);
        when(mockRequest.getStartLine()).thenReturn(mockStartLine);
        when(mockRequest.getStartLine().getRequestUri()).thenReturn("/non-existent-file.html");

        HttpResponse response = new HttpResponse();
        // when
        defaultServlet.service(mockRequest, response);

        // then
        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(response.hasFileBody()).isFalse();
        assertThat(response.getBody()).isNotNull();
    }
}
