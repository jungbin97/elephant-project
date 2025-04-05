package webserver.http11.session;

import java.util.HashMap;
import java.util.Map;

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
