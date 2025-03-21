package logic;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BadgeManager {

    static final String FILE_PATH = "data/badges.csv";

    public List<String> getBadgesFromFile() {
        List<String> badges = new ArrayList<>();

        try (Reader reader = new FileReader(FILE_PATH); CSVParser csvParser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());) {
            for (CSVRecord record : csvParser) {
                String code = record.get("code");

                badges.add(code);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return badges;

    }

    public int countBadges() {
        return getBadgesFromFile().size();
    }
}
