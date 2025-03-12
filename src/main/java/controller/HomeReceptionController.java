package controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import logic.SessionManager;
import logic.VisitManager;
import model.Employee;
import model.Visit;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

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

    @Path("/show-visits")
    @GET
    public TemplateInstance showVisits() {

        List<Visit> visits = VisitManager.getVisitsFromFile();
        return homeReception.data("visits",visits , "type","showVisits" );
    }

    @Path("/filtered-visits")
    @POST
    public Response filterVisits(@FormParam("inputDate") LocalDate inputDate) {

        VisitManager filteredVisits = new VisitManager();
        List<Visit> visits = filteredVisits.getVisitsByDate(inputDate);

        return Response.ok(homeReception.data("visits", visits,"type" , "showVisits")).build();

    }

    @Path("/assign-badge")
    @GET
    public TemplateInstance assignBadge() {

        String badge = ""; // * Funzione per assegnare badge
        return homeReception.data("badge", badge, "type","assignBadge");

    }

    @Path("/get-unfinished-visit")
    @GET
    public TemplateInstance getUnfinishedVisit() {

        VisitManager visits = new VisitManager();
        List<Visit> actualOpenVisits = visits.getUnfinishedVisits();

        return homeReception.data("visits", actualOpenVisits, "type", "closeVisit");
    }



}
