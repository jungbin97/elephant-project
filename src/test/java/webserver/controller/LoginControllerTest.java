package webserver.controller;

import db.DataBase;
import model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginControllerTest {
    private final LoginController loginController = new LoginController();

    @BeforeAll
    static void setUp() {
        User mockUser = new User("test1", "1234", "testName", "test@naver.com");
        DataBase.addUser(mockUser);
    }

    @Test
    @DisplayName("로그인 성공 시 쿠키에 값을 저장하고, index.html로 리다이렉트 해야 한다.")
    void userLoginSuccess() throws Exception {
        // given
        HttpRequest mockRequest = mock(HttpRequest.class);
        when(mockRequest.getQueryParameters()).thenReturn(Map.of(
                "userId", "test1",
                "password", "1234",
                "name", "testName",
                "email", "test@naver.com"
        ));

        HttpResponse response = new HttpResponse();

        // when
        loginController.service(mockRequest, response);

        // then
        assertThat(response.getStatusCode()).isEqualTo(302);
        assertThat(response.getHeader("Location")).isEqualTo("/index.html");
        assertThat(response.getHeader("Set-Cookie")).isEqualTo("logined=true");
    }

    @Test
    @DisplayName("로그인 실패 시 쿠키에 값을 저장하고, user/login_failed.html로 리다이렉트 해야 한다.")
    void userLoginPasswordFail() throws Exception {
        // given
        HttpRequest mockRequest = mock(HttpRequest.class);
        when(mockRequest.getQueryParameters()).thenReturn(Map.of(
                "userId", "test1",
                "password", "1",
                "name", "testName",
                "email", "test@naver.com"
        ));

        HttpResponse response = new HttpResponse();

        // when
        loginController.service(mockRequest, response);

        // then
        assertThat(response.getStatusCode()).isEqualTo(302);
        assertThat(response.getHeader("Location")).isEqualTo("/user/login_failed.html");
        assertThat(response.getHeader("Set-Cookie")).isEqualTo("logined=false");
    }
}
