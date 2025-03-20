package controller;

import io.quarkus.qute.Template;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import logic.*;
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

    private final Template homeEmployee;
    private final SessionManager sessionManager;
    private final GuestManager guestManager;
    private final EmployeeManager employeeManager;
    private final VisitManager visitManager;
    private final FormValidator formValidator;
    private final BadgeManager badgeManager;

    public HomeEmployeeController(Template homeEmployee, SessionManager sessionManager, GuestManager guestManager, EmployeeManager employeeManager, VisitManager visitManager, FormValidator formValidator, BadgeManager badgeManager) {
        this.homeEmployee = homeEmployee;
        this.sessionManager = sessionManager;
        this.guestManager = guestManager;
        this.employeeManager = employeeManager;
        this.visitManager = visitManager;
        this.formValidator = formValidator;
        this.badgeManager = badgeManager;
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
                List<Visit> visits = visitManager.getVisitsFromFile();
                List<Visit> filteredVisits = visitManager.filterVisitsByEmployeeId(visits, employee.getId());
                filteredVisits.sort(Comparator.comparing(Visit::getDate));
                return Response.ok(homeEmployee.data(
                        "employee", employee,
                        "type", "homePage",
                        "visits", visitManager.changeIdsInSurnames(filteredVisits, guestManager, employeeManager)
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
            @FormParam("email") String email,
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

        if(errorMessage == null && !formValidator.checkStringForm(email)){
            errorMessage = "Email non valida";
        }

        if(errorMessage == null && (!formValidator.checkStringForm(phoneNumber) || !formValidator.checkPhoneNumber(phoneNumber))){
            errorMessage = "Numero di telefono non valido";
        }

        if(errorMessage == null && !formValidator.checkStringForm(role)){
            errorMessage = "Ruolo non valido";
        }

        if(errorMessage == null && !formValidator.checkStringForm(company)){
            errorMessage = "Azienda non valida";
        }

        String newId = ""+guestManager.getNewId();
        Guest guest = new Guest(newId, name, surname, email, phoneNumber, role, company);

        if (errorMessage == null && !guestManager.isGuestAlreadyExisting(guest)) {
            errorMessage = "Esiste già un ospite con questa email";
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

        guestManager.saveGuest(guest);

        String successMessage = "Guest successfully added";
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
            errorMessage = "La data non può essere vuota";
        }

        if (errorMessage == null && !formValidator.checkDate(date)) {
            errorMessage = "La visita deve essere aggiunta almeno un giorno in anticipo";
        }

        if (errorMessage == null && !formValidator.checkTimeNotNull(expectedStart)) {
            errorMessage = "L'ora di inizio non può essere vuota";
        }

        if (errorMessage == null && !formValidator.checkTimeNotNull(expectedEnd)) {
            errorMessage = "L'ora di fine non può essere vuota";
        }

        if(errorMessage == null && formValidator.checkTimeIsValid(expectedStart, expectedEnd)) {
            errorMessage = "L'ora di inizio non deve essere successiva a quella di fine";
        }

        if(errorMessage == null && !formValidator.checkStringForm(guestId)){
            errorMessage = "Ospite non valido";
        }

        List<Visit> visitsOfDate = visitManager.getVisitsByDate(date);
        int countOverlapVisits = 0;

        for(Visit visit : visitsOfDate) {
            if (visit.getExpectedStartingHour().isBefore(expectedEnd) && visit.getExpectedEndingHour().isAfter(expectedStart)) {
                countOverlapVisits++;
            }
        }

        if(countOverlapVisits == badgeManager.countBadges()) {
            errorMessage = "Non ci sono più badge disponibili";
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
            String successMessage = "Visita aggiunta correttamente";

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
                    "errorMessage", "Esiste già un altra visita con questi dati",
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
                "visits", visitManager.changeIdsInSurnames(filteredVisits, guestManager, employeeManager),
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
                "visits", visitManager.changeIdsInSurnames(visitsByEmployeeId, guestManager, employeeManager),
                "type", "deleteVisit",
                "errorMessage", null,
                "successMessage", null
        )).build();
    }
}
