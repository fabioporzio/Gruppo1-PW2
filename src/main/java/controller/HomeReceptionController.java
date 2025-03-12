package controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import logic.SessionManager;
import model.Employee;

import java.net.URI;

import static logic.SessionManager.NAME_COOKIE_SESSION;

@Path("/home-reception")
public class HomeReceptionController {

    private final Template homeReception;
    private final SessionManager sessionManager;

    public HomeReceptionController(Template homeReception, SessionManager sessionManager) {
        this.homeReception = homeReception;
        this.sessionManager = sessionManager;
    }

    @GET
    public Response getHomeReception(@CookieParam(NAME_COOKIE_SESSION) String sessionId) {
        if (sessionId != null) {
            Employee employee = sessionManager.getEmployeeFromSession(sessionId);

            if (employee == null) {
                return Response.seeOther(URI.create("/home")).build();
            } else {
                return Response.ok(homeReception.data("employee", employee)).build();
            }
        }
        return Response.seeOther(URI.create("/")).build();
    }

    /*@Path("/show-visits")
    @GET
    public TemplateInstance showVisits() {
        return homeReception.data();
    }*/


}
