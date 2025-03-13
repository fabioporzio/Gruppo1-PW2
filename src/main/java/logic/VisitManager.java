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

    public List<Visit> getVisitsFromFile() {
        String filePath = "data/visits.csv";

        List<Visit> visits = new ArrayList<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());)
        {
            for (CSVRecord record : csvParser) {
                String id = record.get("id");
                LocalDate date = LocalDate.parse(record.get("date"), dateFormatter);
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

        visits.sort(Comparator.comparing(Visit::getDate));
        return visits;
    }

    public void saveVisit(Visit visit) {
        String filePath = "data/visits.csv";

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

    public List<Visit> getVisitsByEmployeeId(Employee employee) {
        List<Visit> visits = getVisitsFromFile();

        for (Visit visit : visits) {
            if (visit.getEmployeeId().equals(employee.getId())) {
                visits.add(visit);
            }
        }

        return visits;
    }

    public List<Visit> getUnfinishedVisits() {
        List<Visit> visits = getVisitsFromFile();

        for (Visit visit : visits) {
            if (visit.getActualStartingHour() != null && visit.getActualEndingHour() == null) {
                visits.add(visit);
            }
        }

        return visits;
    }

    public int getNewId(){
        List<Visit> visits = getVisitsFromFile();

        if (visits.isEmpty()) {
            return 1;
        }
        else {
            return Integer.parseInt(visits.getLast().getId()) + 1;
        }
    }
}
