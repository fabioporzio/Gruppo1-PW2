package logic;

import jakarta.enterprise.context.ApplicationScoped;
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
import java.util.List;

@ApplicationScoped
public class GuestManager {
    private final String filePath = "data/guests.csv";

    public List<Guest> getGuestsFromFile() {
        List<Guest> guests = new ArrayList<>();

        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());)
        {
            for (CSVRecord record : csvParser) {
                String id = record.get("id");
                String name = record.get("name");
                String surname = record.get("surname");
                String role = record.get("role");
                String company = record.get("company");

                Guest guest = new Guest(id, name, surname, role, company);
                guests.add(guest);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return guests;
    }

    public void addGuest(Guest guest) {
        try (Writer writer = new FileWriter(filePath, true);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL.withHeader("id", "name", "surname", "role", "company")))
        {
            csvPrinter.printRecord(
                    guest.getId(),
                    guest.getName(),
                    guest.getSurname(),
                    guest.getRole(),
                    guest.getCompany()
            );
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //Da testare
    public List<Guest> getGuestsByVisitDate(LocalDate visitDate) {
        List<Visit> filteredVisits = new ArrayList<>();
        List<String> guestsIds = new ArrayList<>();
        List<Guest> filteredGuests = new ArrayList<>();

        List<Visit> loadedVisits = VisitManager.getVisitsFromFile();
        List<Guest> loadedGuests = getGuestsFromFile();

        for (Visit loadedVisit : loadedVisits) {
            if(loadedVisit.getDate().equals(visitDate)) {
                guestsIds.add(loadedVisit.getId());
            }
        }

        for (Guest loadedGuest : loadedGuests) {
            if(guestsIds.contains(loadedGuest.getId())) {
                filteredGuests.add(loadedGuest);
            }
        }

        return filteredGuests;
    }
}
