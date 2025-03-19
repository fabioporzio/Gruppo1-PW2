package controller;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import io.quarkus.qute.Template;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import logic.BadgeManager;
import logic.EmployeeManager;
import logic.GuestManager;
import logic.SessionManager;
import static logic.SessionManager.NAME_COOKIE_SESSION;
import logic.VisitManager;
import model.Employee;
import model.Guest;
import model.visit.Visit;
import model.visit.VisitStatus;
import utilities.validation.FormValidator;

@Path("/home-reception")
public class HomeReceptionController {

    private final Template homeReception;
    private final SessionManager sessionManager;
    private final VisitManager visitManager;
    private final FormValidator formValidator;
    private final GuestManager guestManager;
    private final EmployeeManager employeeManager;
    private final BadgeManager badgeManager;

    public HomeReceptionController(Template homeReception, SessionManager sessionManager, VisitManager visitManager, FormValidator formValidator, GuestManager guestManager, EmployeeManager employeeManager, BadgeManager badgeManager) {
        this.homeReception = homeReception;
        this.sessionManager = sessionManager;
        this.visitManager = visitManager;
        this.formValidator = formValidator;
        this.guestManager = guestManager;
        this.employeeManager = employeeManager;
        this.badgeManager = badgeManager;
    }

    /***
     * Redirects to the login page if the session is invalid.
     * Redirects to the badge assignment page if the session is valid.
     *
     * @param sessionId the session cookie
     * @return a redirect response
     */
    @GET
    public Response getHomeReception(@CookieParam(NAME_COOKIE_SESSION) String sessionId) {
        if (sessionId != null) {
            Employee employee = sessionManager.getEmployeeFromSession(sessionId);

            if (employee == null) {
                return Response.seeOther(URI.create("/")).build();
            } else {
                return Response.seeOther(URI.create("/home-reception/assign-badge")).build();
            }
        }
        return Response.seeOther(URI.create("/")).build();
    }

    /***
     * Displays all visits sorted by date.
     *
     * @return the HTML response with the visits list
     */
    @Path("/show-visits")
    @GET
    public Response showVisits(@CookieParam(NAME_COOKIE_SESSION) String sessionId) {
        List<Visit> visits = visitManager.getVisitsFromFile();
        visits.sort(Comparator.comparing(Visit::getDate));

        if (sessionId != null) {
            Employee employee = sessionManager.getEmployeeFromSession(sessionId);
            return Response.ok(homeReception.data(
                    "employee",employee,
                    "visits", visitManager.changeIdsInSurnames(visits, guestManager, employeeManager),
                    "type","showVisits",
                    "date", null
            )).build();
        }
        return Response.seeOther(URI.create("/")).build();

    }

    /***
     * Filters visits by a specific date.
     *
     * @param inputDate the selected date
     * @return the HTML response with filtered visits
     */
    @Path("/filtered-visits")
    @POST
    public Response filterVisits(@FormParam("inputDate") LocalDate inputDate , @CookieParam(NAME_COOKIE_SESSION) String sessionId) {

        List<Visit> visits = visitManager.getVisitsByDate(inputDate);
        if (sessionId != null) {
            Employee employee = sessionManager.getEmployeeFromSession(sessionId);
            return Response.ok(homeReception.data(
                    "employee",employee,
                    "visits", visitManager.changeIdsInSurnames(visits, guestManager, employeeManager),
                    "type" , "showVisits",
                    "date", inputDate
            )).build();
        }
        return Response.seeOther(URI.create("/")).build();
    }

    /***
     * Displays unstarted visits for badge assignment.
     *
     * @return the HTML response with visit list
     */
    @Path("/assign-badge")
    @GET
    public Response showAssignBadge(@CookieParam(NAME_COOKIE_SESSION) String sessionId) {
        if (sessionId != null) {
            List<Visit> unstartedVisits = visitManager.getUnstartedVisitsByDate(LocalDate.now());
            Employee employee = sessionManager.getEmployeeFromSession(sessionId);

            return Response.ok(homeReception.data(
                    "type", "assignBadge",
                    "employee", employee,
                    "errorMessage", null,
                    "successMessage", null,
                    "visits", visitManager.changeIdsInSurnames(unstartedVisits, guestManager, employeeManager)
            )).build();
        }

        

       return null;

    }

    /***
     * Assigns a badge to a visit if available.
     *
     * @param badgeCode the badge code
     * @param visitId   the visit ID
     * @return a redirect or an error response
     */
    @Path("/assign-badge")
    @POST
    public Response assignBadge(
            @FormParam("badge") String badgeCode,
            @FormParam("visitId") String visitId,
            @CookieParam(NAME_COOKIE_SESSION) String sessionId
    ){
        List<String> badges = badgeManager.getBadgesFromFile();
        String errorMessage = null;

        if(!formValidator.checkStringForm(badgeCode)){
            errorMessage = "No code badge was input";
        }

        boolean badgeStatus = false;
        for(String badge:badges){
            if(badge.equals(badgeCode)){
                badgeStatus = true;
                break;
            }
        }

        if (errorMessage == null && !badgeStatus) {
            errorMessage = "This badge code is already assigned";
        }

        List<Visit> unfinishedVisits = visitManager.getUnfinishedVisits();

        for(Visit visit : unfinishedVisits){
            if(errorMessage == null && visit.getBadgeCode().equals(badgeCode)){
                errorMessage = "This badge code is not available";
                break;
            }
        }

        if (sessionId != null) {
            Employee employee = sessionManager.getEmployeeFromSession(sessionId);
            if(errorMessage != null){
                return Response.ok(homeReception.data(
                        "employee",employee,
                        "type", "assignBadge",
                        "errorMessage", errorMessage,
                        "successMessage", null,
                        "visits", visitManager.changeIdsInSurnames(visitManager.getUnstartedVisitsByDate(LocalDate.now()), guestManager, employeeManager)
                )).build();
            }
        }

        List<Visit> visits = visitManager.getVisitsFromFile();

        for(Visit visit : visits){
            if(visit.getId().equals(visitId)){
                visit.setBadgeCode(badgeCode);
                visit.setActualStartingHour(LocalTime.parse(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))));
                visit.setStatus(VisitStatus.STARTED);
            }
        }

        boolean status = visitManager.overwriteVisits(visits);
        if (!status){
            Employee employee = sessionManager.getEmployeeFromSession(sessionId);
            errorMessage = "An error occurred while trying to assign badge";
            return Response.ok(homeReception.data(
                    "employee",employee,
                    "type", "assignBadge",
                    "errorMessage", errorMessage,
                    "successMessage", null,
                    "visits", visitManager.changeIdsInSurnames(visitManager.getUnstartedVisitsByDate(LocalDate.now()), guestManager, employeeManager)
            )).build();
        }

        String successMessage = "Badge code assigned correctly";

        return Response.ok(homeReception.data(
                "type", "assignBadge",
                "errorMessage", null,
                "successMessage", successMessage,
                "visits", visitManager.changeIdsInSurnames(visitManager.getUnstartedVisitsByDate(LocalDate.now()), guestManager, employeeManager)
        )).build();
    }

    /***
     * Displays visits that can be closed.
     *
     * @return the HTML response with unfinished visits
     */
    @Path("/close-visit")
    @GET
    public Response showUnfinishedVisit(@CookieParam(NAME_COOKIE_SESSION) String sessionId) {

        List<Visit> unfinishedVisits = visitManager.getUnfinishedVisits();

        Employee employee = sessionManager.getEmployeeFromSession(sessionId);

        return Response.ok(homeReception.data(
                "employee",employee,
                "visits", visitManager.changeIdsInSurnames(unfinishedVisits, guestManager, employeeManager),
                "type", "closeVisit")
        ).build();
    }

    /***
     * Closes a visit by setting its actual ending time.
     *
     * @param visitId the ID of the visit to close
     * @return a redirect or an error response
     */
    @Path("/close-visit")
    @POST
    public Response closeVisit(@FormParam("visitId") String visitId,
                               @CookieParam(NAME_COOKIE_SESSION) String sessionId){

        List<Visit> visits = visitManager.getVisitsFromFile();

        for(Visit visit : visits){
            if(visit.getId().equals(visitId)){
                visit.setActualEndingHour(LocalTime.parse(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))));
                visit.setStatus(VisitStatus.ENDED);
            }
        }

        boolean status = visitManager.overwriteVisits(visits);
        if (!status && sessionId != null){
            Employee employee = sessionManager.getEmployeeFromSession(sessionId);
            return Response.ok(homeReception.data(
                    "employee",employee,
                    "type", "closeVisit",
                    "errorMessage", "An error occurred while trying to close a visit",
                    "successMessage", null,
                    "visits", visitManager.changeIdsInSurnames(visitManager.getUnfinishedVisits(), guestManager, employeeManager)
            )).build();
        }
        return Response.seeOther(URI.create("home-reception/close-visit")).build();
    }

    /***
     * Displays the form to add a new guest.
     *
     * @param sessionId the session cookie
     * @return the HTML response with the guest form
     */
    @GET
    @Path("/add-guest")
    public Response showFormAddGuest(@CookieParam(NAME_COOKIE_SESSION) String sessionId) {
        Employee employee = sessionManager.getEmployeeFromSession(sessionId);
        return Response.ok(homeReception.data(
                "employee",employee,
                "type", "addGuest",
                "errorMessage", null,
                "successMessage", null,
                "visits", null
        )).build();
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
            errorMessage = "Name is not valid";
        }

        if(errorMessage == null && !formValidator.checkStringForm(surname)){
            errorMessage = "Surname is not valid";
        }

        if(errorMessage == null && !formValidator.checkStringForm(email)){
            errorMessage = "Email is not valid";
        }

        if(errorMessage == null && !formValidator.checkStringForm(phoneNumber)){
            errorMessage = "Phone number is not valid";
        }

        if(errorMessage == null && !formValidator.checkStringForm(role)){
            errorMessage = "Role is not valid";
        }

        if (errorMessage == null && !formValidator.checkStringForm(company)){
            errorMessage = "Company is not valid";
        }

        String newId = ""+guestManager.getNewId();
        Guest guest = new Guest(newId, name, surname, email, phoneNumber, role, company);

        if (errorMessage == null && !guestManager.isGuestAlreadyExisting(guest)) {
            errorMessage = "The guest already exists";
        }

        Employee employee = sessionManager.getEmployeeFromSession(sessionId);
        if(errorMessage != null){

            return Response.ok(homeReception.data(
                    "employee", employee,
                    "type", "addGuest",
                    "errorMessage", errorMessage,
                    "successMessage", null,
                    "visits", null
            )).build();
        }

        guestManager.saveGuest(guest);
        String successMessage = "Guest successfully added";

        return Response.ok(homeReception.data(
                "employee", employee,
                "type", "addGuest",
                "errorMessage", null,
                "successMessage", successMessage,
                "visits", null
        )).build();
    }

    /***
     * Displays the form to add a new visit.
     *
     * @param sessionId the session cookie
     * @return the HTML response with guest list
     */
    @GET
    @Path("/add-visit")
    public Response showFormAddVisit(
            @CookieParam(NAME_COOKIE_SESSION) String sessionId
    ){
        Employee employee = sessionManager.getEmployeeFromSession(sessionId);
        List<Guest> guests = guestManager.getGuestsFromFile();
        List<Employee> employees = employeeManager.getEmployeesFromFile();
        HashMap<String, Object> responseData = new HashMap<>();
        responseData.put("type", "addVisit");
        responseData.put("errorMessage", null);
        responseData.put("successMessage", null);
        responseData.put("guests", guests);
        responseData.put("employees", employees);
        responseData.put("employee", employee);

        return Response.ok(responseData).build();
    }

    /***
     * Adds a new visit if there are available badges.
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
            @FormParam("guest") String guestId,
            @FormParam("employee") String employeeId
    ) {
        List<Guest> guests = guestManager.getGuestsFromFile();
        List<Employee> employees = employeeManager.getEmployeesFromFile();
        String errorMessage = null;

        if (!formValidator.checkDateNotNull(date)) {
            errorMessage = "Date field cannot be empty";
        }

        if (errorMessage == null && !formValidator.checkDate(date)) {
            errorMessage = "The visit must added at least one day prior to the date";
        }

        if (errorMessage == null && !formValidator.checkTimeNotNull(expectedStart)) {
            errorMessage = "The starting hour field cannot be empty";
        }

        if (errorMessage == null && !formValidator.checkTimeNotNull(expectedEnd)) {
            errorMessage = "The ending hour field cannot be empty";
        }

        if(errorMessage == null && formValidator.checkTimeIsValid(expectedStart, expectedEnd)) {
            errorMessage = "Starting hour must be before ending hour";
        }

        if(errorMessage == null && !formValidator.checkStringForm(guestId)){
            errorMessage = "Guest is not valid";
        }

        if(errorMessage == null && !formValidator.checkStringForm(employeeId)){
            errorMessage = "Employee is not valid";
        }

        List<Visit> visitsOfDate = visitManager.getVisitsByDate(date);
        int countOverlapVisits = 0;

        for(Visit visit : visitsOfDate){
            if (visit.getExpectedStartingHour().isBefore(expectedEnd) && visit.getExpectedEndingHour().isAfter(expectedStart)) {
                countOverlapVisits++;
            }
        }

        if(countOverlapVisits == badgeManager.countBadges()){
            errorMessage = "There aren't any more badges available";
        }

        if(errorMessage != null){
            Employee employee = sessionManager.getEmployeeFromSession(sessionId);
            HashMap<String, Object> responseData = new HashMap<>();
            responseData.put("type", "addVisit");
            responseData.put("errorMessage", errorMessage);
            responseData.put("successMessage", null);
            responseData.put("guests", guests);
            responseData.put("employees", employees);
            responseData.put("employee", employee);
            return Response.ok(responseData).build();
        }

        String newId = ""+visitManager.getNewId();
        LocalTime actualStart = LocalTime.ofSecondOfDay(0);
        LocalTime actualEnd = LocalTime.ofSecondOfDay(0);
        VisitStatus visitStatus = VisitStatus.YET_TO_START;

        Employee employee = sessionManager.getEmployeeFromSession(sessionId);

        Visit visit = new Visit(newId, date, expectedStart, actualStart, expectedEnd, actualEnd, visitStatus ,guestId, employeeId, null);
        boolean status = visitManager.saveVisit(visit);

        if (status) {
            String successMessage = "Visit successfully added";
            HashMap<String, Object> responseData = new HashMap<>();
            responseData.put("type","addVisit");
            responseData.put("errorMessage", null);
            responseData.put("successMessage", successMessage);
            responseData.put("guests", guests);
            responseData.put("employees", employees);
            responseData.put("employee", employee);

            return Response.ok(responseData).build();
        }
        else{
            HashMap<String, Object> responseData = new HashMap<>();
            responseData.put("type","addVisit");
            responseData.put("errorMessage", "An other visit already exists");
            responseData.put("successMessage", null);
            responseData.put("guests", guests);
            responseData.put("employees", employees);
            responseData.put("employee", employee);
            return Response.ok(responseData).build();
        }
    }

    /**
     * Displays the list of visits that can be deleted.
     *
     * @param sessionId the session cookie
     * @return the HTML response with the visit list
     */
    @GET
    @Path("/delete-visit")
    public Response showDeleteVisit(@CookieParam(NAME_COOKIE_SESSION) String sessionId) {
        Employee employee = sessionManager.getEmployeeFromSession(sessionId);

        List<Visit> visits = visitManager.getUnstartedVisits();
        visits.sort(Comparator.comparing(Visit::getDate));

        return Response.ok(homeReception.data(
                "type", "deleteVisit",
                "errorMessage", null,
                "successMessage", null,
                "visits", visits,
                "employee", employee
        )).build();
    }

    /**
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
        filteredVisits.sort(Comparator.comparing(Visit::getDate));

        return Response.ok(homeReception.data(
                "employee", employee,
                "type", "deleteVisit",
                "errorMessage", null,
                "successMessage", null,
                "visits", visits
        )).build();
    }
}
