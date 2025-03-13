package logic;

import jakarta.enterprise.context.ApplicationScoped;
import model.Guest;
import model.visit.Visit;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class GuestManager {
    final String filePath = "data/guests.csv";
    final VisitManager visitManager;

    public GuestManager(VisitManager visitManager) {
        this.visitManager = visitManager;
    }


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

    public void saveGuest(Guest guest) {

        try (Writer writer = new FileWriter(filePath, true);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL))
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

        List<Visit> loadedVisits = visitManager.getVisitsFromFile();
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

    public int getNewId(){
        List<Guest> guests = getGuestsFromFile();

        if (guests.isEmpty()) {
            return 1;
        }
        else {
            return Integer.parseInt(guests.getLast().getId()) + 1;
        }
    }
}
