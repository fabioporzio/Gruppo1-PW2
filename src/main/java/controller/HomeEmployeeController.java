package controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import logic.GuestManager;
import logic.SessionManager;
import logic.VisitManager;
import model.Employee;
import model.Guest;
import model.Visit;
import utilities.validation.CredentialsValidator;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static logic.SessionManager.NAME_COOKIE_SESSION;

@Path("/home-employee")
public class HomeEmployeeController {

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
                return Response.ok(homeEmployee.data("employee", employee)).build();
            }
        }
        return Response.seeOther(URI.create("/")).build();

    }

    @GET
    @Path("/add-guest")
    public TemplateInstance showFormAddGuest(){
        return homeEmployee.data("type", "addGuest");
    }

    @POST
    @Path("/add-guest")
    public Response addGuest(
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
            return Response.ok(homeEmployee.data("message", errorMessage, "type", "addGuest")).build();
        }

        String newId = ""+guestManager.getNewId();

        Guest guest = new Guest(newId, name, surname, role, company);

        guestManager.saveGuest(guest);

        return Response.ok(homeEmployee.data("message", "Guest saved", "type", "addGuest")).build();
    }

    @GET
    @Path("/add-visit")
    public TemplateInstance showFormAddVisit(){

        List<Guest> guests = guestManager.getGuestsFromFile();

        return homeEmployee.data("guests", guests, "type", "addVisit");
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
        String errorMessage = null;

        if (!credentialsValidator.checkDate(date)) {
            errorMessage = "The visit's must be at least one day prior";
        }

        if(errorMessage != null){
            return Response.ok(homeEmployee.data("message", errorMessage, "type", "addVisit")).build();
        }

        String newId = ""+visitManager.getNewId();
        String employeeId = sessionManager.getEmployeeFromSession(sessionId).getId();

        Visit visit = new Visit(newId, date, expectedStart, null, expectedEnd, null, guestId, employeeId, null);

        visitManager.saveVisit(visit);

        return Response.ok(homeEmployee.data("message", "Visit saved", "type", "addVisit")).build();
    }
}
