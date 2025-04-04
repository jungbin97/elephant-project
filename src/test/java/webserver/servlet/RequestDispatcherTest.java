package webserver.servlet;

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
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        HttpRequest mockRequest = mock(HttpRequest.class);
        HttpRequestStartLine mockStartLine = mock(HttpRequestStartLine.class);

        when(mockRequest.getStartLine()).thenReturn(mockStartLine);
        when(mockRequest.getStartLine().getRequestUri()).thenReturn("/user/create");
        HttpResponse response = new HttpResponse();

        // when
        dispatcherServlet.service(mockRequest, response);

        // then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(302);
        Assertions.assertThat(response.getHeader("Location")).isEqualTo("/index.html");
    }
}
