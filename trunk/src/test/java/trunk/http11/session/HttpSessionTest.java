package trunk.http11.session;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HttpSessionTest {
    @Test
    @DisplayName("session 저장소에 session ID에 해당하는 세션이 없다면 새로운 세션을 생성한다.")
    void createNewSession() {
        // given
        String sessionId = "test";

        // when
        SessionManager.getSession(sessionId, true);

        // then
        assertThat(SessionManager.getSession(sessionId, true)).isNotNull();
    }


    @Test
    @DisplayName("session id에 해당하는 session에 attribute를 저장하고 가져올 수 있다.")
    void setAndGetAttribute() {
        // given
        String sessionId = "test";
        String attributeName = "userId";
        String attributeValue = "user123";

        // when
        HttpSession session = SessionManager.getSession(sessionId, true);
        session.setAttribute(attributeName, attributeValue);

        // then
        assertThat(session.getAttribute(attributeName)).isEqualTo(attributeValue);
    }

    @Test
    @DisplayName("session id에 해당하는 session에 attribute를 삭제할 수 있다.")
    void removeAttribute() {
        // given
        String sessionId = "test";
        String attributeName = "userId";
        String attributeValue = "user123";

        // when
        HttpSession session = SessionManager.getSession(sessionId, true);
        session.setAttribute(attributeName, attributeValue);
        session.removeAttribute(attributeName);

        // then
        assertThat(session.getAttribute(attributeName)).isNull();
    }
}
