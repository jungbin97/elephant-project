package webserver.http11.session;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code HttpSessions}는 애플리케이션 내에서 세션 객체({@link HttpSession})를 저장하고 관리하는 역할을 담당합니다.
 * <p>
 * - 클라이언트의 {@code JSESSIONID} 값을 기반으로 세션을 조회하거나,<br>
 * - 세션이 존재하지 않을 경우 조건에 따라 새로 생성합니다.<br>
 * - 세션 삭제 또한 지원합니다.
 * <p>
 * 내부적으로는 {@code Map<String, HttpSession>} 구조를 사용하여 세션을 저장하며,<br>
 * 세션 ID는 일반적으로 {@code UUID} 기반으로 생성되어 전달됩니다.
 *
 * <h2>스레드 안전성</h2>
 * 이 클래스는 현재 {@code HashMap}을 사용하고 있으므로 멀티스레드 환경에서는 동시성 문제가 발생할 수 있습니다.
 * 실 운영 환경에서는 {@code ConcurrentHashMap}으로 대체하거나, 동기화 처리를 명시적으로 해주어야 합니다.
 *
 * @see HttpSession
 * @author jungbin97
 */
public class HttpSessions {
    private static final Map<String, HttpSession> sessions = new HashMap<>();

    private HttpSessions() {
    }

    public static HttpSession getSession(String id, boolean create) {
        HttpSession session = sessions.get(id);
        // 만약 세션이 존재하지 않고, 세션을 생성해야 한다면
        if (session == null && create) {
            session = new HttpSession(id);
            sessions.put(id, session);
        }

        return session;
    }

    public static void removeSession(String id) {
        sessions.remove(id);
    }
}
