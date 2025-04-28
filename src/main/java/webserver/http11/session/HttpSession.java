package webserver.http11.session;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * {@code HttpSession}은 클라이언트와 서버 간의 세션을 관리하는 클래스입니다.
 * <p>
 * - 세션 ID는 {@code UUID} 기반으로 세션을 식별합니다.<br>
 * - 세션에 저장된 속성(attribute)을 관리할 수 있는 메서드를 제공합니다.<br>
 * - 세션을 무효화(invalidate)할 수 있는 메서드를 제공합니다.
 *
 * @see HttpSessions
 * @author jungbin97
 */
public class HttpSession {
    private final String id;
    private final Map<String, Object> attributes = new HashMap<>();

    public HttpSession(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public void invalidate() {
        HttpSessions.removeSession(id);
    }
}
