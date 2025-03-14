package controller;

import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import logic.SessionManager;

import java.net.URI;

import static logic.SessionManager.NAME_COOKIE_SESSION;

@Path("/logout")
public class LogoutController {

    private final SessionManager sessionManager;

    public LogoutController(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @GET
    public Response processLogout(
            @CookieParam(NAME_COOKIE_SESSION) String sessionId
    ) {
        sessionManager.removeSession(sessionId);

        return Response.seeOther(URI.create("/"))
                .cookie(new NewCookie.Builder(NAME_COOKIE_SESSION).value(null).build())
                .build();
    }
}
