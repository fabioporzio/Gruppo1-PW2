package logic;

import jakarta.enterprise.context.ApplicationScoped;
import model.visit.Visit;
import model.visit.VisitStatus;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class VisitManager {

    static final String filePath = "data/visits.csv";

    /***
     * Reads visits from the file and returns a list of all visits.
     *
     * This method parses the `visits.csv` file, extracts each visit's details such as
     * date, expected and actual start/end times, visit status, guest and employee IDs,
     * and badge code. It then returns a list of `Visit` objects.
     *
     * @return A list of `Visit` objects parsed from the file.
     */
    public List<Visit> getVisitsFromFile() {

        List<Visit> visits = new ArrayList<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());)
        {
            for (CSVRecord record : csvParser) {
                String id = record.get("id");
                LocalDate date = LocalDate.parse(record.get("date").trim(), dateFormatter);
                LocalTime expectedStartingHour = LocalTime.parse(record.get("expected_starting_hour"), timeFormatter);
                LocalTime actualStartingHour = LocalTime.parse(record.get("actual_starting_hour"), timeFormatter);
                LocalTime expectedEndingHour = LocalTime.parse(record.get("expected_ending_hour"), timeFormatter);
                LocalTime actualEndingHour = LocalTime.parse(record.get("actual_ending_time"), timeFormatter);
                VisitStatus visitStatus = VisitStatus.valueOf(record.get("visit_status"));
                String guestId = record.get("guest_id");
                String employeeId = record.get("employee_id");
                String badgeCode = record.get("badge_code");

                Visit visit = new Visit(id, date, expectedStartingHour, actualStartingHour, expectedEndingHour, actualEndingHour, visitStatus, guestId, employeeId, badgeCode);
                visits.add(visit);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return visits;
    }

    /***
     * Saves a new visit to the file.
     *
     * This method adds a new visit to the `visits.csv` file. It checks for duplicates
     * before saving the visit, and returns `true` if the visit is successfully saved,
     * or `false` if a duplicate visit is found.
     *
     * @param visit The visit to be saved.
     * @return `true` if the visit is saved successfully, `false` if it's a duplicate.
     */
    public boolean saveVisit(Visit visit) {

        if (!checkDouble(visit)){
            try (Writer writer = new FileWriter(filePath, true);
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL))
            {
                csvPrinter.printRecord(
                        visit.getId(),
                        visit.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        visit.getExpectedStartingHour().format(DateTimeFormatter.ofPattern("HH:mm")),
                        visit.getActualStartingHour().format(DateTimeFormatter.ofPattern("HH:mm")),
                        visit.getExpectedEndingHour().format(DateTimeFormatter.ofPattern("HH:mm")),
                        visit.getActualEndingHour().format(DateTimeFormatter.ofPattern("HH:mm")),
                        visit.getStatus().name(),
                        visit.getGuestId(),
                        visit.getEmployeeId(),
                        visit.getBadgeCode()
                );
                return true;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Filters visits by a specific date.
     *
     * This method filters and returns all visits that match the provided date.
     *
     * @param date The date to filter visits by.
     * @return A list of visits that match the provided date.
     */
    public List<Visit> getVisitsByDate(LocalDate date) {
        List<Visit> visits = getVisitsFromFile();
        List<Visit> filteredVisits = new ArrayList<>();

        for (Visit visit : visits) {
            if (visit.getDate().equals(date)) {
                filteredVisits.add(visit);
            }
        }

        return filteredVisits;
    }

    /***
     * Filters visits by the employee ID.
     *
     * This method filters and returns all visits that were conducted by the specified
     * employee based on the provided employee ID.
     *
     * @param employeeId The ID of the employee to filter visits by.
     * @return A list of visits conducted by the specified employee.
     */
    public List<Visit> getVisitsByEmployeeId(String employeeId) {
        List<Visit> visits = getVisitsFromFile();

        List<Visit> filteredVisits = new ArrayList<>();

        for (Visit visit : visits) {
            if (visit.getEmployeeId().equals(employeeId)) {
                filteredVisits.add(visit);
            }
        }

        return filteredVisits;
    }

    /***
     * Filters and returns visits that have not yet ended.
     *
     * This method checks for visits where the actual ending hour is still set to the
     * default value ("00:00") and the visit has already started. It returns a list of
     * such unfinished visits.
     *
     * @return A list of unfinished visits.
     */
    public List<Visit> getUnfinishedVisits() {
        List<Visit> visits = getVisitsFromFile();

        List<Visit> filteredVisits = new ArrayList<>();

        for (Visit visit : visits) {
            if (visit.getActualStartingHour() != LocalTime.parse("00:00")
                    && visit.getActualEndingHour() == LocalTime.parse("00:00")) {
                filteredVisits.add(visit);
            }
        }

        return filteredVisits;
    }

    /***
     * Filters and returns visits that have not yet started.
     *
     * This method checks for visits where both the actual starting and ending hours
     * are still set to the default value ("00:00"). It returns a list of such unstarted visits.
     *
     * @return A list of unstarted visits.
     */
    public List<Visit> getUnstartedVisits() {
        List<Visit> visits = getVisitsFromFile();

        List<Visit> filteredVisits = new ArrayList<>();

        for (Visit visit : visits) {
            if (visit.getActualStartingHour() == LocalTime.parse("00:00")
                    && visit.getActualEndingHour() == LocalTime.parse("00:00")) {
                filteredVisits.add(visit);
            }
        }

        return filteredVisits;
    }

    /***
     * Retrieves a visit by its ID.
     *
     * This method searches for a visit with the specified ID in the list of all visits.
     * If the visit is found, it is returned; otherwise, `null` is returned.
     *
     * @param inputVisitId The ID of the visit to retrieve.
     * @return The `Visit` object if found, `null` if not found.
     */
    public Visit getVisitById(String inputVisitId) {
        List<Visit> visits = getVisitsFromFile();

        for (Visit visit : visits) {
            if (visit.getId().equals(inputVisitId)) {
                return visit;
            }
        }
        return null;
    }

    /***
     * Filters out the specified visit from the list of all visits.
     *
     * This method filters out the given visit from the list of all visits based on
     * the visit's ID, and returns the remaining visits.
     *
     * @param visit The visit to be excluded from the result list.
     * @return A list of all visits excluding the specified visit.
     */
    public List<Visit> getFilteredVisits(Visit visit) {
        List<Visit> visits = getVisitsFromFile();
        List<Visit> filteredVisits = new ArrayList<>();

        for (Visit visit1 : visits) {
            if (visit1.getId().equals(visit.getId())) {
                continue;
            }
            else {
                filteredVisits.add(visit1);
            }
        }

        return filteredVisits;
    }

    /***
     * Overwrites the entire list of visits in the file.
     *
     * This method overwrites the `visits.csv` file with the provided list of visits.
     * It saves all visits in the new list, including their details such as date, start/end
     * times, status, and IDs.
     *
     * @param visits The list of visits to be saved to the file.
     * @return `true` if the overwrite operation is successful, `false` if it fails.
     */
    public boolean overwriteVisits(List<Visit> visits) {

        try(FileWriter writer = new FileWriter(filePath);
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL.withHeader("id", "date", "expected_starting_hour", "actual_starting_hour", "expected_ending_hour", "actual_ending_time", "visit_status", "guest_id", "employee_id", "badge_code")))
        {
            for (Visit newVisit : visits) {
                csvPrinter.printRecord(
                        newVisit.getId(),
                        newVisit.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        newVisit.getExpectedStartingHour().format(DateTimeFormatter.ofPattern("HH:mm")),
                        newVisit.getActualStartingHour().format(DateTimeFormatter.ofPattern("HH:mm")),
                        newVisit.getExpectedEndingHour().format(DateTimeFormatter.ofPattern("HH:mm")),
                        newVisit.getActualEndingHour().format(DateTimeFormatter.ofPattern("HH:mm")),
                        newVisit.getStatus().name(),
                        newVisit.getGuestId(),
                        newVisit.getEmployeeId(),
                        newVisit.getBadgeCode()
                );
            }
            return true;
        }
        catch (IOException e){
            e.printStackTrace();
            return false;
        }

    }

    /***
     * Generates a new unique visit ID.
     *
     * This method calculates the next available ID by checking the last visit in the
     * list of visits and incrementing the ID by one. If there are no visits, it returns 1.
     *
     * @return The next available visit ID.
     */
    public int getNewId(){
        List<Visit> visits = getVisitsFromFile();

        if (visits.isEmpty()) {
            return 1;
        }
        else {
            return Integer.parseInt(visits.getLast().getId()) + 1;
        }
    }

    /***
     * Checks if a visit is a duplicate based on its details.
     *
     * This method compares the provided visit with existing visits to check if a visit
     * with the same date, starting and ending hours, guest, and employee IDs already exists.
     * If a duplicate is found, it returns `true`; otherwise, it returns `false`.
     *
     * @param visit The visit to be checked for duplicates.
     * @return `true` if a duplicate visit is found, `false` otherwise.
     */
    public boolean checkDouble(Visit visit){
        List<Visit> visits = getVisitsFromFile();

        for(Visit v : visits){
            if(v.getDate().equals(visit.getDate()) && v.getExpectedStartingHour().equals(visit.getExpectedStartingHour()) &&
                v.getExpectedEndingHour().equals(visit.getExpectedEndingHour()) && v.getGuestId().equals(visit.getGuestId()) &&
                v.getEmployeeId().equals(visit.getEmployeeId())){

                return true;
            }
        }
        return false;
    }
}
