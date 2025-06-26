package mvc.controller;

import mvc.db.DataBase;
import mvc.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import trunk.http11.request.HttpRequest;
import trunk.http11.response.HttpResponse;
import trunk.http11.session.HttpSession;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LoginControllerTest {
    private final LoginController loginController = new LoginController();

    @BeforeAll
    static void setUp() {
        User mockUser = new User("test1", "1234", "testName", "test@naver.com");
        DataBase.addUser(mockUser);
    }

    @Test
    @DisplayName("로그인 성공 시 세션에 값을 저장하고, index.html로 리다이렉트 해야 한다.")
    void userLoginSuccess() throws Exception {
        // given
        HttpRequest mockRequest = mock(HttpRequest.class);
        HttpSession mockSession = mock(HttpSession.class);
        when(mockRequest.getQueryParameters()).thenReturn(Map.of(
                "userId", "test1",
                "password", "1234",
                "name", "testName",
                "email", "test@naver.com"
        ));
        when(mockRequest.getSession()).thenReturn(mockSession);
        HttpResponse response = new HttpResponse();

        // when
        loginController.service(mockRequest, response);

        // then
        assertThat(response.getStatusCode()).isEqualTo(302);
        assertThat(response.getHeader("Location")).isEqualTo("/index.html");
        verify(mockSession).setAttribute(eq("user"), any(User.class));
    }


    @Test
    @DisplayName("로그인 실패 시 세션에 값을 저장하고, user/login_failed.html로 리다이렉트 해야 한다.")
    void userLoginPasswordFail() throws Exception {
        // given
        HttpRequest mockRequest = mock(HttpRequest.class);
        HttpSession mockSession = mock(HttpSession.class);
        when(mockRequest.getQueryParameters()).thenReturn(Map.of(
                "userId", "test1",
                "password", "1",
                "name", "testName",
                "email", "test@naver.com"
        ));
        when(mockRequest.getSession()).thenReturn(mockSession);

        HttpResponse response = new HttpResponse();

        // when
        loginController.service(mockRequest, response);

        // then
        assertThat(response.getStatusCode()).isEqualTo(302);
        assertThat(response.getHeader("Location")).isEqualTo("/user/login_failed.html");
        verify(mockSession, never()).setAttribute(eq("user"), any());
    }
}
