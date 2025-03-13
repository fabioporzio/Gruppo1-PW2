package controller;

import io.quarkus.qute.Template;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
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
import java.util.List;

import static logic.SessionManager.NAME_COOKIE_SESSION;

@Path("/home-employee")
public class HomeEmployeeController {

    private final static int MAX_BADGE = 2;

    private final Template homeEmployee;
    private final SessionManager sessionManager;
    private final GuestManager guestManager;
    private final VisitManager visitManager;
    private final CredentialsValidator credentialsValidator;

    public HomeEmployeeController(Template homeEmployee, SessionManager sessionManager, GuestManager guestManager, VisitManager visitManager, CredentialsValidator credentialsValidator) {
        this.homeEmployee = homeEmployee;
        this.sessionManager = sessionManager;
        this.guestManager = guestManager;
        this.visitManager = visitManager;
        this.credentialsValidator = credentialsValidator;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getHome(
            @CookieParam(NAME_COOKIE_SESSION) String sessionId
    ) {
        if (sessionId != null) {
            Employee employee = sessionManager.getEmployeeFromSession(sessionId);

            if (employee == null) {
                return Response.seeOther(URI.create("/")).build();
            } else {
                NewCookie sessionCookie = sessionManager.getSession(sessionId);
                return Response.ok(homeEmployee.data(
                        "employee", employee,
                        "type", null
                )).cookie(sessionCookie).build();
            }
        }
        return Response.seeOther(URI.create("/")).build();

    }

    @GET
    @Path("/add-guest")
    public Response showFormAddGuest(@CookieParam(NAME_COOKIE_SESSION) String sessionId) {
        NewCookie sessionCookie = sessionManager.getSession(sessionId);
        return Response.ok(homeEmployee.data(
                "type", "addGuest",
                "errorMessage", null,
                "successMessage", null
        )).cookie(sessionCookie).build();
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
        NewCookie sessionCookie = sessionManager.getSession(sessionId);
        String errorMessage = null;

        if(!credentialsValidator.checkStringForm(name)){
            errorMessage = "Name is not valid";
        }

        if(!credentialsValidator.checkStringForm(surname)){
            errorMessage = "Surname is not valid";
        }

        if(errorMessage != null){
            return Response.ok(homeEmployee.data(
                    "errorMessage", errorMessage,
                    "successMessage", null,
                    "type", "addGuest"
            )).cookie(sessionCookie).build();
        }

        String newId = ""+guestManager.getNewId();
        Guest guest = new Guest(newId, name, surname, role, company);
        guestManager.saveGuest(guest);

        String successMessage = "Successfully added guest";

        return Response.ok(homeEmployee.data(
                "errorMessage", null,
                "successMessage", successMessage,
                "type", "addGuest"
        )).cookie(sessionCookie).build();
    }

    @GET
    @Path("/add-visit")
    public Response showFormAddVisit(
            @CookieParam(NAME_COOKIE_SESSION) String sessionId
    ){
        List<Guest> guests = guestManager.getGuestsFromFile();

        NewCookie sessionCookie = sessionManager.getSession(sessionId);
        return Response.ok(homeEmployee.data(
                "guests", guests,
                "type", "addVisit",
                "errorMessage", null,
                "successMessage", null
        )).cookie(sessionCookie).build();
    }

    @POST
    @Path("/add-visit")
    public Response addVisit(
            @CookieParam(NAME_COOKIE_SESSION) String sessionId,
            @FormParam("date") LocalDate date,
            @FormParam("expectedStart") LocalTime expectedStart,
            @FormParam("expectedEnd") LocalTime expectedEnd,
            @FormParam("guest") String guestId
    ){
        NewCookie sessionCookie = sessionManager.getSession(sessionId);

        List<Guest> guests = guestManager.getGuestsFromFile();
        String errorMessage = null;

        if (!credentialsValidator.checkDate(date)) {
            errorMessage = "The visit must be at least one day prior";
        }

        if(expectedStart.isAfter(expectedEnd) || expectedStart.equals(expectedEnd)){
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
            return Response.ok(homeEmployee.data(
                    "errorMessage", errorMessage,
                    "successMessage", null,
                    "guests", guests,
                    "type", "addVisit"
            )).cookie(sessionCookie).build();
        }

        String newId = ""+visitManager.getNewId();
        LocalTime actualStart = LocalTime.ofSecondOfDay(0);
        LocalTime actualEnd = LocalTime.ofSecondOfDay(0);
        VisitStatus visitStatus = VisitStatus.YET_TO_START;

        System.out.println("Session ID nel post: " + sessionId);

        Employee employee = sessionManager.getEmployeeFromSession(sessionId);
        String employeeId = employee.getId();

        System.out.println(employeeId);

        Visit visit = new Visit(newId, date, expectedStart, actualStart, expectedEnd, actualEnd, visitStatus ,guestId, employeeId, null);
        visitManager.saveVisit(visit);

        String successMessage = "Successfully added visit";

        return Response.ok(homeEmployee.data(
                "successMessage", successMessage,
                "errorMessage", null,
                "guests", guests,
                "type", "addVisit"
        )).cookie(sessionCookie).build();
    }

    @GET
    @Path("/delete-visit")
    public Response deleteVisit(@CookieParam(NAME_COOKIE_SESSION) String sessionId){
        NewCookie sessionCookie = sessionManager.getSession(sessionId);
        Employee employee = sessionManager.getEmployeeFromSession(sessionId);

        List<Visit> visits = visitManager.getVisitsByEmployeeId(employee.getId());
        return Response.ok(homeEmployee.data(
                "visits", visits,
                "type", "deleteVisit",
                "errorMessage", null,
                "successMessage", null
        )).cookie(sessionCookie).build();
    }
}
