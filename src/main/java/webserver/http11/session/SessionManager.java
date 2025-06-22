package webserver.http11.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 애플리케이션의 모든 {@link HttpSession} 객체를 저장하고 관리하는 중앙 저장소입니다.
 * <p>
 * 이 클래스는 정적(static) 메서드만으로 구성된 유틸리티 클래스로,
 * 클라이언트의 JSESSIONID를 키로 사용하여 세션을 조회, 생성, 삭제하는 기능을 제공합니다.
 *
 * <h2>스레드 안전성 (Thread-Safety)</h2>
 * 여러 스레드에서 동시에 세션 맵에 접근하는 것을 안전하게 처리하기 위해,
 * 내부적으로 {@link ConcurrentHashMap}을 사용합니다. 이를 통해 별도의
 * 동기화(synchronization) 블록 없이도 동시성 제공합니다.
 *
 * @author jungbin97
 * @see HttpSession
 */
public class SessionManager {
    private static final Map<String, HttpSession> sessions = new ConcurrentHashMap<>();

    /**
     * 이 클래스는 유틸리티 클래스로, 인스턴스화할 수 없습니다.
     */
    private SessionManager() {
    }

    /**
     * 지정된 세션 ID에 해당하는 {@link HttpSession}을 반환합니다.
     * <p>
     * 세션이 존재하지 않을 경우, {@code create} 파라미터 값에 따라 새로운 세션을 생성할지 결정합니다.
     *
     * @param id     조회할 세션의 ID
     * @param create 세션이 존재하지 않을 때 새로 생성할지 여부.
     * {@code true}이면 세션을 생성하고, {@code false}이면 {@code null}을 반환합니다.
     * @return 조회되거나 새로 생성된 {@code HttpSession}. 또는, {@code create}가 {@code false}이고 세션이 없는 경우 {@code null}.
     */
    public static HttpSession getSession(String id, boolean create) {
        HttpSession session = sessions.get(id);
        // 만약 세션이 존재하지 않고, 세션을 생성해야 한다면
        if (session == null && create) {
            session = new HttpSession(id);
            sessions.put(id, session);
        }

        return session;
    }

    /**
     * 지정된 ID의 세션을 저장소에서 제거합니다.
     *
     * @param id 제거할 세션의 ID
     */
    public static void removeSession(String id) {
        sessions.remove(id);
    }
}
