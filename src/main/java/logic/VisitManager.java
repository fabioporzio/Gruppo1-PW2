package logic;

import model.Employee;
import model.Guest;
import model.Visit;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class VisitManager {

    public static List<Visit> getVisitsFromFile() {
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
                String guestId = record.get("guest_id");
                String employeeId = record.get("employee_id");
                String badgeCode = record.get("badge_code");

                Visit visit = new Visit(id, date, expectedStartingHour, actualStartingHour, expectedEndingHour, actualEndingHour, guestId, employeeId, badgeCode);
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
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL.withHeader("id", "date", "expected_starting_hour", "actual_starting_hour", "expected_ending_hour", "actual_ending_time", "guest_id", "employee_id", "badge_code")))
        {
            csvPrinter.printRecord(
                    visit.getId(),
                    visit.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    visit.getExpectedStartingHour().format(DateTimeFormatter.ofPattern("HH:mm")),
                    visit.getActualStartingHour().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                    visit.getExpectedEndingHour().format(DateTimeFormatter.ofPattern("HH:mm")),
                    visit.getActualEndingHour().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
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

        return Integer.parseInt(visits.getLast().getId()) + 1;
    }
}
