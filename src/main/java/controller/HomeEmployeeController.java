package controller;

import io.quarkus.qute.Template;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import logic.GuestManager;
import logic.SessionManager;
import logic.VisitManager;
import model.Employee;
import model.Guest;
import model.visit.Visit;
import model.visit.VisitStatus;
import utilities.validation.FormValidator;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

import static logic.SessionManager.NAME_COOKIE_SESSION;

@Path("/home-employee")
public class HomeEmployeeController {

    private final static int MAX_BADGE = 2;

    private final Template homeEmployee;
    private final SessionManager sessionManager;
    private final GuestManager guestManager;
    private final VisitManager visitManager;
    private final FormValidator formValidator;

    public HomeEmployeeController(Template homeEmployee, SessionManager sessionManager, GuestManager guestManager, VisitManager visitManager, FormValidator formValidator) {
        this.homeEmployee = homeEmployee;
        this.sessionManager = sessionManager;
        this.guestManager = guestManager;
        this.visitManager = visitManager;
        this.formValidator = formValidator;
    }


    /***
     * Displays the employee home page.
     * Redirects to the login page if the session is invalid.
     *
     * @param sessionId the session cookie
     * @return the HTML response with the employee home page
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getHome(
            @CookieParam(NAME_COOKIE_SESSION) String sessionId
    ) {

        if (sessionId != null) {
            Employee employee = sessionManager.getEmployeeFromSession(sessionId);

            if (employee == null) {
                return Response.seeOther(URI.create("/")).build();
            }
            else {
                return Response.ok(homeEmployee.data(
                        "employee", employee,
                        "type", null
                )).build();
            }
        }
        return Response.seeOther(URI.create("/")).build();

    }


    /***
     * Shows the form to add a new guest.
     *
     * @param sessionId the session cookie
     * @return the HTML response with the guest form
     */
    @GET
    @Path("/add-guest")
    public Response showFormAddGuest(@CookieParam(NAME_COOKIE_SESSION) String sessionId) {
        if (sessionId != null) {
            Employee employee = sessionManager.getEmployeeFromSession(sessionId);
            return Response.ok(homeEmployee.data(
                "employee", employee,
                "type", "addGuest",
                "errorMessage", null,
                "successMessage", null
        )).build();
        }
        return Response.seeOther(URI.create("/")).build();
    }

    /***
     * Adds a new guest after validating the input.
     *
     * @param sessionId the session cookie
     * @param name      the guest's first name
     * @param surname   the guest's last name
     * @param phoneNumber   the guest's phone number
     * @param role      the guest's role
     * @param company   the guest's company
     * @return the HTML response with success or error messages
     */
    @POST
    @Path("/add-guest")
    public Response addGuest(
            @CookieParam(NAME_COOKIE_SESSION) String sessionId,
            @FormParam("name") String name,
            @FormParam("surname") String surname,
            @FormParam("phoneNumber") String phoneNumber,
            @FormParam("role") String role,
            @FormParam("company") String company
    ){

        String errorMessage = null;

        if(!formValidator.checkStringForm(name)){
            errorMessage = "Nome non valido";
        }

        if(errorMessage == null && !formValidator.checkStringForm(surname)){
            errorMessage = "Cognome non valido";
        }

        if(errorMessage == null && !formValidator.checkStringForm(phoneNumber)){
            errorMessage = "Numero di telefono non valido";
        }

        if(errorMessage == null && !formValidator.checkStringForm(role)){
            errorMessage = "Ruolo non valido";
        }

        if(errorMessage == null && !formValidator.checkStringForm(company)){
            errorMessage = "Azienda non valida";
        }
        if (sessionId != null) {
            Employee employee = sessionManager.getEmployeeFromSession(sessionId);
            if(errorMessage != null){
                return Response.ok(homeEmployee.data(
                        "employee",employee,
                        "errorMessage", errorMessage,
                        "successMessage", null,
                        "type", "addGuest"
                )).build();
            }
        }

        String newId = "" + guestManager.getNewId();
        Guest guest = new Guest(newId, name, surname, phoneNumber, role, company);
        guestManager.saveGuest(guest);

        String successMessage = "Ospite inserito";
        if (sessionId != null) {
            Employee employee = sessionManager.getEmployeeFromSession(sessionId);
            return Response.ok(homeEmployee.data(
                    "employee", employee,
                    "errorMessage", null,
                    "successMessage", successMessage,
                    "type", "addGuest"
            )).build();
        }
        return Response.seeOther(URI.create("/")).build();
    }

    /***
     * Shows the form to add a new visit.
     *
     * @param sessionId the session cookie
     * @return the HTML response with the visit form
     */
    @GET
    @Path("/add-visit")
    public Response showFormAddVisit(
            @CookieParam(NAME_COOKIE_SESSION) String sessionId
    ){

        List<Guest> guests = guestManager.getGuestsFromFile();
        if (sessionId != null) {
            Employee employee = sessionManager.getEmployeeFromSession(sessionId);
            return Response.ok(homeEmployee.data(
                    "employee",employee,
                    "guests", guests,
                    "type", "addVisit",
                    "errorMessage", null,
                    "successMessage", null
            )).build();
        }
        return Response.seeOther(URI.create("/")).build();
    }

    /***
     * Adds a new visit after checking date, time, and badge availability.
     *
     * @param sessionId     the session cookie
     * @param date          the visit date
     * @param expectedStart the expected start time
     * @param expectedEnd   the expected end time
     * @param guestId       the guest's ID
     * @return the HTML response with success or error messages
     */
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

        if (!formValidator.checkDateNotNull(date)) {
            errorMessage = "Data non può essere vuota";
        }

        if (errorMessage == null && !formValidator.checkDate(date)) {
            errorMessage = "La visita deve essere inserita almeno un giorno prima";
        }

        if (errorMessage == null && !formValidator.checkTimeNotNull(expectedStart)) {
            errorMessage = "L'ora di inizio non può essere vuota";
        }

        if (errorMessage == null && !formValidator.checkTimeNotNull(expectedEnd)) {
            errorMessage = "L'ora di inizio non può essere vuota";
        }

        if(errorMessage == null && formValidator.checkTimeIsValid(expectedStart, expectedEnd)) {
            errorMessage = "L'ora di inizio deve essere prima dell'ora di fine";
        }

        if(errorMessage == null && !formValidator.checkStringForm(guestId)){
            errorMessage = "Azienda non valida";
        }

        List<Visit> visitsOfDate = visitManager.getVisitsByDate(date);
        int countOverlapVisits = 0;

        for(Visit visit : visitsOfDate) {
            if (visit.getExpectedStartingHour().isBefore(expectedEnd) && visit.getExpectedEndingHour().isAfter(expectedStart)) {
                countOverlapVisits++;
            }
        }

        if(countOverlapVisits == MAX_BADGE) {
            errorMessage = "I badge sono terminati";
        }
        if (sessionId != null) {
            Employee employee = sessionManager.getEmployeeFromSession(sessionId);

            if(errorMessage != null){
                return Response.ok(homeEmployee.data(
                        "employee",employee,
                        "errorMessage", errorMessage,
                        "successMessage", null,
                        "guests", guests,
                        "type", "addVisit"
                )).build();
            }
        }

        String newId = "" + visitManager.getNewId();
        LocalTime actualStart = LocalTime.ofSecondOfDay(0);
        LocalTime actualEnd = LocalTime.ofSecondOfDay(0);
        VisitStatus visitStatus = VisitStatus.YET_TO_START;

        Employee employee = sessionManager.getEmployeeFromSession(sessionId);
        String employeeId = employee.getId();

        Visit visit = new Visit(newId, date, expectedStart, actualStart, expectedEnd, actualEnd, visitStatus ,guestId, employeeId, null);
        boolean status = visitManager.saveVisit(visit);

        if (status) {
            String successMessage = "Visita aggiunta";

            return Response.ok(homeEmployee.data(
                    "employee",employee,
                    "successMessage", successMessage,
                    "errorMessage", null,
                    "guests", guests,
                    "type", "addVisit"
            )).build();
        }
        else {
            return Response.ok(homeEmployee.data(
                    "employee",employee,
                    "successMessage", null,
                    "errorMessage", "Esiste gia un altra visita aggiunta",
                    "guests", guests,
                    "type", "addVisit"
            )).build();
        }
    }

    /***
     * Shows the list of visits that can be deleted.
     *
     * @param sessionId the session cookie
     * @return the HTML response with the list of visits
     */
    @GET
    @Path("/delete-visit")
    public Response showDeleteVisit(@CookieParam(NAME_COOKIE_SESSION) String sessionId) {
        Employee employee = sessionManager.getEmployeeFromSession(sessionId);

        List<Visit> visits = visitManager.getUnstartedVisits();
        List<Visit> filteredVisits = visitManager.filterVisitsByEmployeeId(visits, employee.getId());
        filteredVisits.sort(Comparator.comparing(Visit::getDate));

        return Response.ok(homeEmployee.data(
                "employee",employee,
                "visits", filteredVisits,
                "type", "deleteVisit",
                "errorMessage", null,
                "successMessage", null
        )).build();
    }

    /***
     * Deletes a visit based on the given visit ID.
     *
     * @param sessionId the session cookie
     * @param visitId   the ID of the visit to delete
     * @return the HTML response with success or error messages
     */
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

        List<Visit> visits = visitManager.getUnstartedVisits();
        List<Visit> visitsByEmployeeId = visitManager.filterVisitsByEmployeeId(visits, employee.getId());
        filteredVisits.sort(Comparator.comparing(Visit::getDate));

        return Response.ok(homeEmployee.data(
                "employee",employee,
                "visits", visitsByEmployeeId,
                "type", "deleteVisit",
                "errorMessage", null,
                "successMessage", null
        )).build();
    }
}
