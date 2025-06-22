package webserver.http11.session;

import webserver.http11.request.HttpRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 특정 클라이언트와 서버 간의 세션 상태를 저장하고 관리하는 객체입니다.
 * <p>
 * 이 객체는 사용자의 로그인 정보, 장바구니 내용 등 사용자와 관련된 데이터를
 * 서버 측에 유지하기 위해 사용됩니다. 각 세션은 고유한 ID({@link #id})를 가지며,
 * 이 ID는 보통 클라이언트의 쿠키에 저장되어 요청 시마다 서버로 전달됩니다.
 * <p>
 * 세션에 데이터를 저장하거나 조회하기 위해 {@code get/set/removeAttribute} 메서드를,
 * 세션을 만료시키기 위해 {@link #invalidate()} 메서드를 사용합니다.
 *
 * @see HttpSessions
 * @see HttpRequest#getSession()
 */
public class HttpSession {
    private final String id;
    private final Map<String, Object> attributes = new HashMap<>();

    /**
     * 지정된 세션 ID로 새로운 HttpSession 객체를 생성합니다.
     * 이 생성자는 일반적으로 {@link HttpSessions}에 의해 내부적으로 호출됩니다.
     *
     * @param id 이 세션을 식별하는 고유한 ID (일반적으로 {@link UUID} 사용)
     */
    public HttpSession(String id) {
        this.id = id;
    }

    /**
     * 이 세션의 고유 ID를 반환합니다.
     *
     * @return 세션 ID 문자열
     */
    public String getId() {
        return id;
    }

    /**
     * 이 세션에 지정된 이름으로 객체(데이터)를 바인딩(저장)합니다.
     * 같은 이름으로 이미 바인딩된 객체가 있다면, 새로운 객체로 덮어씁니다.
     *
     * @param name  데이터를 바인딩할 이름
     * @param value 바인딩할 객체(데이터)
     */
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    /**
     * 이 세션에서 지정된 이름으로 바인딩된 객체를 반환합니다.
     *
     * @param name 조회할 데이터의 이름
     * @return 바인딩된 객체. 해당 이름으로 바인딩된 객체가 없으면 {@code null}을 반환합니다.
     */
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * 이 세션에서 지정된 이름으로 바인딩된 객체를 제거합니다.
     *
     * @param name 제거할 데이터의 이름
     */
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    /**
     * 이 세션을 무효화하고, 세션에 바인딩된 모든 데이터를 제거합니다.
     * <p>
     * 내부적으로 {@link HttpSessions}의 정적 메서드를 호출하여
     * 중앙 세션 저장소에서 이 세션 객체를 제거하도록 요청합니다.
     */
    public void invalidate() {
        HttpSessions.removeSession(id);
    }
}
