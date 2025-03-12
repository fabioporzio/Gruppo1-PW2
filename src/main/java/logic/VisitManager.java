package logic;

import jakarta.enterprise.context.ApplicationScoped;
import model.Visit;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class VisitManager {

    final String path = "data/visits";

    public List<Visit> getVisitsFromFileFiltered(LocalDate inputDate) throws FileNotFoundException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String stringDate = inputDate.format(formatter);

        LocalDate formattedDate = LocalDate.parse(stringDate, formatter);

        List<Visit> visits = new ArrayList<>();

        try (Reader reader = new FileReader(path);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());) {

            for (CSVRecord record : csvParser) {

                String id = record.get("id");

                String dateString = record.get("date");
                LocalDate date = LocalDate.parse(dateString, formatter);

                String stringStartTime = record.get("start_time");
                String stringFinishTime = record.get("finish_time");

                LocalTime startTime = LocalTime.parse(stringStartTime, timeFormatter);
                LocalTime finishTime = LocalTime.parse(stringFinishTime, timeFormatter);


                String badge = record.get("badge");

                if (date.equals(formattedDate)) {
                    Visit visit = new Visit(id,date,startTime,finishTime,badge);
                    visits.add(visit);
                }

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return visits;
    }
}
