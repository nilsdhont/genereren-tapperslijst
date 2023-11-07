package be.brigandze;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TappersManager {

    public static List<Tapper> readTappersFromCSV(){
        try {
            Path path = Paths.get(ClassLoader.getSystemResource("club_members.csv").toURI());
            try (CSVReader reader = new CSVReader(new FileReader(path.toString()))) {
                return reader.readAll().stream().map(TappersManager::lineToTapper).collect(Collectors.toList());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Tapper lineToTapper(String[] line) {
        Tapper tapper = new Tapper();
        tapper.setNaam(line[0]);
        tapper.setPloeg(Ploeg.valueOf(line[1]));
        tapper.setTrainer(Boolean.valueOf(line[2]));
        return tapper;
    }
}
