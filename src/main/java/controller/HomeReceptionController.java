package controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import logic.EmployeeManager;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private final EmployeeManager employeeManager;

    public HomeReceptionController(Template homeReception, SessionManager sessionManager, VisitManager visitManager, CredentialsValidator credentialsValidator, GuestManager guestManager, EmployeeManager employeeManager) {
        this.homeReception = homeReception;
        this.sessionManager = sessionManager;
        this.visitManager = visitManager;
        this.credentialsValidator = credentialsValidator;
        this.guestManager = guestManager;
        this.employeeManager = employeeManager;
    }

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

    @Path("/show-visits")
    @GET
    public TemplateInstance showVisits() {
        List<Visit> visits = visitManager.getVisitsFromFile();
        visits.sort(Comparator.comparing(Visit::getDate));

        return homeReception.data(
                "visits", completeVisits(visits, guestManager, employeeManager),
                "type","showVisits"
        );
    }

    @Path("/filtered-visits")
    @POST
    public Response filterVisits(@FormParam("inputDate") LocalDate inputDate) {

        List<Visit> visits = visitManager.getVisitsByDate(inputDate);

        return Response.ok(homeReception.data(
                "visits", completeVisits(visits, guestManager, employeeManager),
                "type" , "showVisits"
        )).build();
    }

    @Path("/assign-badge")
    @GET
    public TemplateInstance showAssignBadge() {

        List<Visit> unstartedVisits = visitManager.getUnstartedVisits();
        return homeReception.data("visits", completeVisits(unstartedVisits, guestManager, employeeManager), "type","assignBadge");

    }

    @Path("/assign-badge")
    @POST
    public Response assignBadge(
            @FormParam("badge") String badgeCode,
            @FormParam("visitId") String visitId
    ){

        String errorMessage = null;

        if(!credentialsValidator.checkStringForm(badgeCode)){
            errorMessage = "Il badge è vuoto";
        }

        List<Visit> unfinishedVisists = visitManager.getUnfinishedVisits();

        for(Visit visit : unfinishedVisists){
            if(visit.getBadgeCode().equals(badgeCode)){
                errorMessage = "Questo badge non è disponibile";
                break;
            }
        }

        if(errorMessage != null){
            return Response.ok(homeReception.data(
                    "type", "assignBadge",
                    "errorMessage", errorMessage,
                    "successMessage", null,
                    "visits", null
            )).build();
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
            errorMessage = "Errore nel savlare il badge";
            return Response.ok(homeReception.data(
                    "type", "assignBadge",
                    "errorMessage", errorMessage,
                    "successMessage", null,
                    "visits", visitManager.getUnstartedVisits()
            )).build();
        }
        return Response.seeOther(URI.create("home-reception/assign-badge")).build();
    }

    @Path("/close-visit")
    @GET
    public TemplateInstance showUnfinishedVisit() {

        List<Visit> unfinishedVisits = visitManager.getUnfinishedVisits();

        return homeReception.data("visits", unfinishedVisits, "type", "closeVisit");
    }

    @Path("/close-visit")
    @POST
    public Response closeVisit(@FormParam("visitId") String visitId){

        List<Visit> visits = visitManager.getVisitsFromFile();

        for(Visit visit : visits){
            if(visit.getId().equals(visitId)){
                visit.setActualEndingHour(LocalTime.parse(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))));
                visit.setStatus(VisitStatus.ENDED);
            }
        }

        boolean status = visitManager.overwriteVisits(visits);
        if (!status){
            return Response.ok(homeReception.data(
                    "type", "closeVisit",
                    "errorMessage", "Errore nel chiudere la visita",
                    "successMessage", null,
                    "visits", visitManager.getUnfinishedVisits()
            )).build();
        }
        return Response.seeOther(URI.create("home-reception/close-visit")).build();
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
            errorMessage = "Il nome non è valido";
        }

        if(!credentialsValidator.checkStringForm(surname)){
            errorMessage = "Il cognome non è valido";
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

        String successMessage = "Ospite aggiunto";

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
            errorMessage = "L'orario previsto di inizio deve essere prima dell'orario previsto di fine.";
        }

        List<Visit> visitsOfDate = visitManager.getVisitsByDate(date);
        int countOverlapVisits = 0;

        for(Visit visit : visitsOfDate){
            if (visit.getExpectedStartingHour().isBefore(expectedEnd) && visit.getExpectedEndingHour().isAfter(expectedStart)) {
                countOverlapVisits++;
            }
        }

        if(countOverlapVisits == MAX_BADGE){
            errorMessage = "I badge sono terminati";
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
        boolean status = visitManager.saveVisit(visit);

        if (status) {
            String successMessage = "Visita aggiunta";

            return Response.ok(homeReception.data(
                    "type", "addVisit",
                    "errorMessage", null,
                    "successMessage", successMessage,
                    "guests", guests,
                    "visits", null
            )).build();
        }
        else{
            return Response.ok(homeReception.data(
                    "type", "addVisit",
                    "errorMessage", "Esiste gia un altra visita aggiunta",
                    "successMessage", null,
                    "guests", guests,
                    "visits", null
            )).build();
        }
    }

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

        List<Visit> visits = visitManager.getUnstartedVisits();
        filteredVisits.sort(Comparator.comparing(Visit::getDate));

        return Response.ok(homeReception.data(
                "type", "deleteVisit",
                "errorMessage", null,
                "successMessage", null,
                "visits", visits
        )).build();
    }

    private static List<Visit> completeVisits(List<Visit> visits, GuestManager guestManager, EmployeeManager employeeManager){

        List<Visit> completedVisits = new ArrayList<>();

        for(Visit visit : visits){
            Guest guest = guestManager.getGuestById(visit.getGuestId());
            Employee employee = employeeManager.getEmployeeById(visit.getEmployeeId());

            visit.setGuestId(guest.getSurname());
            visit.setEmployeeId(employee.getSurname());

            completedVisits.add(visit);
        }
        return completedVisits;
    }

}
