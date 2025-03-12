package controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import logic.SessionManager;
import model.Employee;

import java.net.URI;

import static logic.SessionManager.NAME_COOKIE_SESSION;

@Path("/home")
public class HomeController {

    private final Template home;
    private final SessionManager sessionManager;

    public HomeController(Template home, SessionManager sessionManager) {
        this.home = home;
        this.sessionManager = sessionManager;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getHome(
            @CookieParam(NAME_COOKIE_SESSION) String sessionId
    ) {
        if (sessionId != null) {
            Employee employee = sessionManager.getEmployeeFromSession(sessionId);

            if (employee == null) {
                return Response.seeOther(URI.create("/home")).build();
            } else {
                return Response.ok(home.data("employee", employee)).build();
            }
        }
        return Response.seeOther(URI.create("/")).build();

    }

}
