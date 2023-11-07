package be.brigandze;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Shiften {

    public static List<Shift> readExistingFromCSV(List<Tapper> tappers) {
        try {
            Path path = Paths.get(ClassLoader.getSystemResource("tapperslijst.csv").toURI());
            try (CSVReader reader = new CSVReader(new FileReader(path.toString()))) {
                return reader.readAll().stream().map(line -> Shiften.lineToShift(line, tappers)).collect(Collectors.toList());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }} catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static Shift lineToShift(String[] line, List<Tapper> allTappers) {
        LocalDate startDate = LocalDate.parse(line[0],DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String beschrijving = line[1];
        DayOfWeek dayofweek = Shift.vertaalDayOfWeek(line[2]);
        String[] startAndEndTime = line[3].split(" - ");
        LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.parse(startAndEndTime[0], DateTimeFormatter.ofPattern("HH.mm")));
        LocalTime endTime = LocalTime.parse(startAndEndTime[1], DateTimeFormatter.ofPattern("HH.mm"));
        List<Tapper> tappers = new ArrayList<>();
        for(int i = 4; i < line.length ; i++) {
            int finalI = i;
            Optional<Tapper> first = allTappers.stream().filter(tapper -> tapper.getNaam().equalsIgnoreCase(line[finalI])).findFirst();
            if(first.isPresent()){
                tappers.add(first.get());

            }

        }
        Shift shift = new Shift(startDateTime, endTime, dayofweek, tappers, beschrijving);
        return shift;
    }
}
