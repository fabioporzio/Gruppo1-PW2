package logic;

import jakarta.enterprise.context.ApplicationScoped;
import model.Employee;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class BadgeManager {

    static final String filePath = "data/badges.csv";

    public List<String> getBadgesFromFile() {
        List<String> badges = new ArrayList<>();

        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());)
        {
            for (CSVRecord record : csvParser) {
                String code = record.get("code");

                badges.add(code);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return badges;

    }

    public int countBadges(){
        return getBadgesFromFile().size();
    }
}
