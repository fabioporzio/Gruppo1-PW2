package controller.Web.Validation.session;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.NewCookie;

@ApplicationScoped
public class SessionManager {

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
