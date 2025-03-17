package controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import logic.GuestManager;
import logic.SessionManager;
import logic.VisitManager;
import model.Employee;
import model.Guest;
import model.visit.Visit;
import model.visit.VisitStatus;
import utilities.validation.CredentialsValidator;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

import static logic.SessionManager.NAME_COOKIE_SESSION;

@Path("/home-reception")
public class HomeReceptionController {

    private final static int MAX_BADGE = 2;

    private final Template homeReception;
    private final SessionManager sessionManager;
    private final VisitManager visitManager;
    private final CredentialsValidator credentialsValidator;
    private final GuestManager guestManager;

    public HomeReceptionController(Template homeReception, SessionManager sessionManager, VisitManager visitManager, CredentialsValidator credentialsValidator, GuestManager guestManager) {
        this.homeReception = homeReception;
        this.sessionManager = sessionManager;
        this.visitManager = visitManager;
        this.credentialsValidator = credentialsValidator;
        this.guestManager = guestManager;
    }

    @GET
    public Response getHomeReception(@CookieParam(NAME_COOKIE_SESSION) String sessionId) {
        if (sessionId != null) {
            Employee employee = sessionManager.getEmployeeFromSession(sessionId);

            if (employee == null) {
                return Response.seeOther(URI.create("/")).build();
            } else {
                return Response.ok(homeReception.data("employee", employee, "type" , null, "visits" , null)).build();
            }
        }
        return Response.seeOther(URI.create("/")).build();
    }

    @Path("/show-visits")
    @GET
    public TemplateInstance showVisits() {
        List<Visit> visits = visitManager.getVisitsFromFile();
        visits.sort(Comparator.comparing(Visit::getDate));

        return homeReception.data(
                "visits",visits ,
                "type","showVisits"
        );
    }

    @Path("/filtered-visits")
    @POST
    public Response filterVisits(@FormParam("inputDate") LocalDate inputDate) {

        List<Visit> visits = visitManager.getVisitsByDate(inputDate);

        return Response.ok(homeReception.data(
                "visits", visits,
                "type" , "showVisits"
        )).build();
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

    @GET
    @Path("/add-guest")
    public Response showFormAddGuest(@CookieParam(NAME_COOKIE_SESSION) String sessionId) {

        return Response.ok(homeReception.data(
                "type", "addGuest",
                "errorMessage", null,
                "successMessage", null,
                "visits", null
        )).build();
    }

    @POST
    @Path("/add-guest")
    public Response addGuest(
            @CookieParam(NAME_COOKIE_SESSION) String sessionId,
            @FormParam("name") String name,
            @FormParam("surname") String surname,
            @FormParam("role") String role,
            @FormParam("company") String company
    ){
        String errorMessage = null;

        if(!credentialsValidator.checkStringForm(name)){
            errorMessage = "Name is not valid";
        }

        if(!credentialsValidator.checkStringForm(surname)){
            errorMessage = "Surname is not valid";
        }

        if(errorMessage != null){
            return Response.ok(homeReception.data(
                    "type", "addGuest",
                    "errorMessage", errorMessage,
                    "successMessage", null,
                    "visits", null
            )).build();
        }

        String newId = ""+guestManager.getNewId();
        Guest guest = new Guest(newId, name, surname, role, company);
        guestManager.saveGuest(guest);

        String successMessage = "Successfully added guest";

        return Response.ok(homeReception.data(
                "type", "addGuest",
                "errorMessage", null,
                "successMessage", successMessage,
                "visits", null
        )).build();
    }

    @GET
    @Path("/add-visit")
    public Response showFormAddVisit(
            @CookieParam(NAME_COOKIE_SESSION) String sessionId
    ){
        List<Guest> guests = guestManager.getGuestsFromFile();

        return Response.ok(homeReception.data(
                "type", "addVisit",
                "errorMessage", null,
                "successMessage", null,
                "guests", guests,
                "visits", null
        )).build();
    }

    @POST
    @Path("/add-visit")
    public Response addVisit(
            @CookieParam(NAME_COOKIE_SESSION) String sessionId,
            @FormParam("date") LocalDate date,
            @FormParam("expectedStart") LocalTime expectedStart,
            @FormParam("expectedEnd") LocalTime expectedEnd,
            @FormParam("guest") String guestId
    ) {
        List<Guest> guests = guestManager.getGuestsFromFile();
        String errorMessage = null;

        if(expectedStart.isAfter(expectedEnd) || expectedStart.equals(expectedEnd)) {
            errorMessage = "The expected start must be before the expected end";
        }

        List<Visit> visitsOfDate = visitManager.getVisitsByDate(date);
        int countOverlapVisits = 0;

        for(Visit visit : visitsOfDate){
            if (visit.getExpectedStartingHour().isBefore(expectedEnd) && visit.getExpectedEndingHour().isAfter(expectedStart)) {
                countOverlapVisits++;
            }
        }

        if(countOverlapVisits == MAX_BADGE){
            errorMessage = "For that time slot the schedule is full";
        }

        if(errorMessage != null){
            return Response.ok(homeReception.data(
                    "type", "addVisit",
                    "errorMessage", errorMessage,
                    "successMessage", null,
                    "guests", guests,
                    "visits", null
            )).build();
        }

        String newId = ""+visitManager.getNewId();
        LocalTime actualStart = LocalTime.ofSecondOfDay(0);
        LocalTime actualEnd = LocalTime.ofSecondOfDay(0);
        VisitStatus visitStatus = VisitStatus.YET_TO_START;

        Employee employee = sessionManager.getEmployeeFromSession(sessionId);
        String employeeId = employee.getId();

        Visit visit = new Visit(newId, date, expectedStart, actualStart, expectedEnd, actualEnd, visitStatus ,guestId, employeeId, null);
        visitManager.saveVisit(visit);

        String successMessage = "Successfully added visit";

        return Response.ok(homeReception.data(
                "type", "addVisit",
                "errorMessage", null,
                "successMessage", successMessage,
                "guests", guests,
                "visits", null
        )).build();
    }

    @GET
    @Path("/delete-visit")
    public Response showDeleteVisit(@CookieParam(NAME_COOKIE_SESSION) String sessionId) {
        Employee employee = sessionManager.getEmployeeFromSession(sessionId);

        List<Visit> visits = visitManager.getVisitsByEmployeeId(employee.getId());
        return Response.ok(homeReception.data(
                "type", "deleteVisit",
                "errorMessage", null,
                "successMessage", null,
                "visits", visits
        )).build();
    }

    @POST
    @Path("/delete-visit")
    public Response deleteVisit(
            @CookieParam(NAME_COOKIE_SESSION) String sessionId,
            @FormParam("visitId") String visitId
    ) {
        Employee employee = sessionManager.getEmployeeFromSession(sessionId);

        Visit visit = visitManager.getVisitById(visitId);
        List<Visit> filteredVisits = visitManager.getFilteredVisits(visit);
        visitManager.overwriteVisits(filteredVisits);

        List<Visit> visits = visitManager.getVisitsByEmployeeId(employee.getId());

        return Response.ok(homeReception.data(
                "type", "deleteVisit",
                "errorMessage", null,
                "successMessage", null,
                "visits", visits
        )).build();
    }

}
