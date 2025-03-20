package logic;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import jakarta.enterprise.context.ApplicationScoped;
import model.Guest;
import model.visit.Visit;

@ApplicationScoped
public class GuestManager {

    final String FILE_PATH = "data/guests.csv";
    final VisitManager visitManager;

    public GuestManager(VisitManager visitManager) {
        this.visitManager = visitManager;
    }

    /**
     * *
     * Retrieves a list of guests from the CSV file.
     *
     * This method reads the guest data from the file located at
     * `data/guests.csv`, parses the CSV content, and converts it into a list of
     * Guest objects. Each guest's data is mapped from the CSV columns to the
     * corresponding fields of the Guest class.
     *
     * @return A list of Guest objects representing the guests from the file.
     */
    public List<Guest> getGuestsFromFile() {
        List<Guest> guests = new ArrayList<>();

        try (Reader reader = new FileReader(FILE_PATH); CSVParser csvParser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());) {
            for (CSVRecord record : csvParser) {
                String id = record.get("id");
                String name = record.get("name");
                String surname = record.get("surname");
                String email = record.get("email");
                String phoneNumber = record.get("phone_number");
                String role = record.get("role");
                String company = record.get("company");

                Guest guest = new Guest(id, name, surname, email, phoneNumber, role, company);
                guests.add(guest);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return guests;
    }

    /**
     * *
     * Saves a guest's data to the CSV file.
     *
     * This method appends a new record with the provided guest's information to
     * the CSV file located at `data/guests.csv`. The file is opened in append
     * mode, and the new record is added at the end.
     *
     * @param guest The Guest object containing the information to be saved.
     */
    public void saveGuest(Guest guest) {

        try (Writer writer = new FileWriter(FILE_PATH, true); CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
            csvPrinter.printRecord(
                    guest.getId(),
                    guest.getName(),
                    guest.getSurname(),
                    guest.getEmail(),
                    guest.getPhoneNumber(),
                    guest.getRole(),
                    guest.getCompany()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * *
     * Retrieves a guest by their unique ID.
     *
     * This method searches the list of guests retrieved from the file and
     * returns the Guest object whose ID matches the given ID. If no guest with
     * the given ID is found, the method returns null.
     *
     * @param id The ID of the guest to be retrieved.
     * @return The Guest object corresponding to the given ID, or null if not
     * found.
     */
    public Guest getGuestById(String id) {
        List<Guest> guests = getGuestsFromFile();

        for (Guest guest : guests) {
            if (guest.getId().equals(id)) {
                return guest;
            }
        }
        return null;
    }

    /**
     * *
     * Retrieves a list of guests who visited on a specific date.
     *
     * This method filters the visits and returns the list of guests whose visit
     * date matches the provided date. The method first filters the visits by
     * the given date, then retrieves the corresponding guest information.
     *
     * @param visitDate The visit date to filter guests by.
     * @return A list of Guest objects who visited on the given date.
     */
    //Da testare
    public List<Guest> getGuestsByVisitDate(LocalDate visitDate) {
        List<Visit> filteredVisits = new ArrayList<>();
        List<String> guestsIds = new ArrayList<>();
        List<Guest> filteredGuests = new ArrayList<>();

        List<Visit> loadedVisits = visitManager.getVisitsFromFile();
        List<Guest> loadedGuests = getGuestsFromFile();

        for (Visit loadedVisit : loadedVisits) {
            if (loadedVisit.getDate().equals(visitDate)) {
                guestsIds.add(loadedVisit.getId());
            }
        }

        for (Guest loadedGuest : loadedGuests) {
            if (guestsIds.contains(loadedGuest.getId())) {
                filteredGuests.add(loadedGuest);
            }
        }

        return filteredGuests;
    }

    /**
     * *
     * Retrieves the next available unique ID for a new guest.
     *
     * This method calculates the next available ID by looking at the existing
     * guest records and returning the next number in sequence. If there are no
     * existing guests, it returns 1 as the starting ID.
     *
     * @return The next available ID for a new guest.
     */
    public int getNewId() {
        List<Guest> guests = getGuestsFromFile();

        if (guests.isEmpty()) {
            return 1;
        } else {
            return Integer.parseInt(guests.getLast().getId()) + 1;
        }
    }

    public boolean isGuestAlreadyExisting(Guest Inputguest) {
        List<Guest> guests = getGuestsFromFile();

        for (Guest guest : guests) {
            if (guest.getEmail().equals(Inputguest.getEmail())) {
                return false;
            }
        }
        return true;
    }
}
