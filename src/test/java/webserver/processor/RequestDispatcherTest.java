package webserver.processor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import webserver.http11.request.HttpRequest;
import webserver.http11.request.HttpRequestStartLine;
import webserver.http11.response.HttpResponse;

import static org.mockito.Mockito.*;

class RequestDispatcherTest {

    @Test
    @DisplayName("요청 URI에 따라 적절한 Controller에 요청을 전달해야 한다.")
    void dispatcherToController() throws Exception {
        // given
        RequestDispatcher requestDispatcher = new RequestDispatcher(new StaticResourceProcessor());
        HttpRequest mockRequest = mock(HttpRequest.class);
        HttpRequestStartLine mockStartLine = mock(HttpRequestStartLine.class);

        when(mockRequest.getStartLine()).thenReturn(mockStartLine);
        when(mockRequest.getStartLine().getRequestUri()).thenReturn("/user/create");

        // when
        HttpResponse httpResponse = requestDispatcher.dispatch(mockRequest);

        // then
        Assertions.assertThat(httpResponse.getStatusCode()).isEqualTo(302);
        Assertions.assertThat(httpResponse.getHeader("Location")).isEqualTo("/index.html");
    }

    @Test
    @DisplayName("정적 파일 요청시 StaticResourceProcessor를 통해 처리해야 한다.")
    void dispatcherToStaticResourceProcessor() throws Exception {
        // given
        StaticResourceProcessor mockStaticResourceProcessor = mock(StaticResourceProcessor.class);
        RequestDispatcher requestDispatcher = new RequestDispatcher(mockStaticResourceProcessor);
        HttpRequest mockRequest = mock(HttpRequest.class);
        HttpRequestStartLine mockStartLine = mock(HttpRequestStartLine.class);

        when(mockRequest.getStartLine()).thenReturn(mockStartLine);
        when(mockRequest.getStartLine().getRequestUri()).thenReturn("/index.html");

        HttpResponse mockResponse = new HttpResponse(200, "text/html", "<html>test</html>".getBytes());
        when(mockStaticResourceProcessor.process(mockRequest)).thenReturn(mockResponse);

        // when
        HttpResponse httpResponse = requestDispatcher.dispatch(mockRequest);

        // then
        Assertions.assertThat(httpResponse.getStatusCode()).isEqualTo(200);
        verify(mockStaticResourceProcessor, times(1)).process(mockRequest);
    }
}
