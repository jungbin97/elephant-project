package webserver.controller;

import mvc.db.DataBase;
import mvc.model.User;
import mvc.controller.UserListController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import webserver.http11.request.HttpRequest;
import webserver.http11.request.HttpRequestHeader;
import webserver.http11.response.HttpResponse;
import webserver.http11.session.HttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserListControllerTest {
    private final UserListController userListController = new UserListController();

    @BeforeAll
    static void setUp() {
        DataBase.addUser(new User("user1", "password", "name1", "email1@example.com"));
    }


    @Test
    @DisplayName("사용자가 로그인 되어 있다면 사용자 목록을 조회할 수 있어야 한다.")
    void loginUserList() throws Exception {
        // given
        HttpRequest request = mock(HttpRequest.class);
        HttpRequestHeader header = mock(HttpRequestHeader.class);
        HttpSession HttpSession = mock(HttpSession.class);

        when(request.getHeaders()).thenReturn(header);
        when(request.getSession()).thenReturn(HttpSession);
        when(HttpSession.getAttribute("user")).thenReturn(new User("user1", "password","name1", "email1@example.com"));

        HttpResponse response = new HttpResponse();
        // when
         userListController.service(request, response);

        // then
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getHeader("Content-Type")).isEqualTo("text/html");
        assertThat(new String(response.getBody())).contains("<h1>User List</h1>");
    }

    @Test
    @DisplayName("사용자가 로그인 되어 있지 않다면 로그인 페이지로 리다이렉트 되어야 한다.")
    void notLoggedInUserShouldRedirect() {
        // given
        HttpRequest request = mock(HttpRequest.class);
        HttpRequestHeader header = mock(HttpRequestHeader.class);
        HttpSession httpSession = mock(HttpSession.class);

        when(request.getHeaders()).thenReturn(header);
        when(request.getSession()).thenReturn(httpSession);

        HttpResponse response = new HttpResponse();
        // when
        userListController.service(request, response);

        // then
        assertThat(response.getStatusCode()).isEqualTo(302);
        assertThat(response.getHeader("Location")).isEqualTo("/user/login.html");
    }
}
