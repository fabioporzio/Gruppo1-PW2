package logic;

import jakarta.enterprise.context.ApplicationScoped;
import model.Employee;
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
    public List<Visit> getVisitsFromFile() {

        List<Visit> visits = new ArrayList<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());)
        {
            for (CSVRecord record : csvParser) {
                String id = record.get("id");
                System.out.println(id);
                LocalDate date = LocalDate.parse(record.get("date"), dateFormatter);
                System.out.println(date);
                LocalTime expectedStartingHour = LocalTime.parse(record.get("expected_starting_hour"), timeFormatter);
                System.out.println(expectedStartingHour);
                LocalTime actualStartingHour = LocalTime.parse(record.get("actual_starting_hour"), timeFormatter);
                System.out.println(actualStartingHour);
                LocalTime expectedEndingHour = LocalTime.parse(record.get("expected_ending_hour"), timeFormatter);
                System.out.println(expectedEndingHour);
                LocalTime actualEndingHour = LocalTime.parse(record.get("actual_ending_time"), timeFormatter);
                System.out.println(actualEndingHour);
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

        visits.sort(Comparator.comparing(Visit::getDate));
        return visits;
    }

    public void saveVisit(Visit visit) {


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
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public List<Visit> getVisitsByDate(LocalDate date) {
        List<Visit> visits = getVisitsFromFile();

        for (Visit visit : visits) {
            if (visit.getDate().equals(date)) {
                visits.add(visit);
            }
        }

        return visits;
    }

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

    public List<Visit> getUnfinishedVisits() {
        List<Visit> visits = getVisitsFromFile();

        List<Visit> filteredVisits = new ArrayList<>();

        for (Visit visit : visits) {
            if (visit.getActualStartingHour() != null && visit.getActualEndingHour() == null) {
                filteredVisits.add(visit);
            }
        }

        return filteredVisits;
    }

    public List<Visit> getUnstartedVisits() {
        List<Visit> visits = getVisitsFromFile();

        List<Visit> filteredVisits = new ArrayList<>();

        for (Visit visit : visits) {
            if (visit.getActualStartingHour() == null && visit.getActualEndingHour() == null) {
                filteredVisits.add(visit);
            }
        }

        return filteredVisits;
    }

    public Visit getVisitById(String inputVisitId) {
        List<Visit> visits = getVisitsFromFile();

        for (Visit visit : visits) {
            if (visit.getId().equals(inputVisitId)) {
                return visit;
            }
        }
        return null;
    }

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

    public void overwriteVisits(List<Visit> visits) {

        try(FileWriter writer = new FileWriter(filePath);
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL.withHeader("id", "date", "expected_starting_hour", "actual_starting_hour", "expected_ending_hour", "actual_ending_time", "visit_status", "guest_id", "employee_id", "badge_code")))
        {
            for (Visit visit : visits) {
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
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }



    }

    public int getNewId(){
        List<Visit> visits = getVisitsFromFile();
        System.out.println(visits.size());

        if (visits.isEmpty()) {
            return 1;
        }
        else {
            return Integer.parseInt(visits.getLast().getId()) + 1;
        }
    }

    public void overwriteVisit(List<Visit> visits) throws IOException {

    }
}
