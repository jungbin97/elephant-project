package webserver.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;

import java.util.Map;

import static org.mockito.Mockito.*;

class UserControllerTest {

    @Test
    @DisplayName("회원 가입 요청을 받아 User 객체를 생성하고 index.html로 리다이렉트 해야 한다.")
    void userCreateRedirect() throws Exception {
        // given
        UserController userController = new UserController();
        HttpRequest mockRequest = mock(HttpRequest.class);
        when(mockRequest.getQueryParameters()).thenReturn(Map.of(
                "userId", "test",
                "password", "test",
                "name", "test",
                "email", "test"
        ));

        // when
        HttpResponse response = userController.handle(mockRequest);

        // then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(302);
        Assertions.assertThat(response.getHeader("Location")).isEqualTo("/index.html");

    }
}
