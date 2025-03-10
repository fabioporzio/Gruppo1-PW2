package controller.session;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.NewCookie;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class sessionManager {

    public static final String NAME_COOKIE_SESSION = "Session";

    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    public NewCookie createUserSession(String email){
        String idSession = UUID.randomUUID().toString();
        sessions.put(idSession, email);
        return new NewCookie.Builder(NAME_COOKIE_SESSION).value(idSession).build();
    }

    public String getUserFromSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public void removeUserFromSession(String sessionId) {
        sessions.remove(sessionId);
    }
}
