package webserver.http11.session;

import java.util.HashMap;
import java.util.Map;

public class HttpSessions {
    private static final Map<String, HttpSession> sessions = new HashMap<>();

    private HttpSessions() {
    }

    public static HttpSession getSession(String id) {
        HttpSession session = sessions.get(id);

        // 없으면 세션 생성
        if (session == null) {
            session = new HttpSession(id);
            sessions.put(id, session);
            return session;
        }

        return session;
    }

    public static void removeSession(String id) {
        sessions.remove(id);
    }
}
