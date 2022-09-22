package be.brigandze;

import com.opencsv.CSVWriter;
import org.apache.commons.lang3.ArrayUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    private static List<Shift> shiften = new ArrayList<>();
    private static List<Tapper> tappers;
    private static Map<LocalDate, Ploeg> matchen;

    private static LocalDate startWinterStop = LocalDate.of(2022, 12, 25);
    private static LocalDate endWinterStop = LocalDate.of(2023, 1, 8);

    public static void main(String[] args) {

        vulLedenLijst();
        vulMatchen();
        //        System.out.println("Aantal tappers: " + tappers.size());
        //        System.out.println("Aantal matchen: " + matchen.size());

        LocalDate startDate = LocalDate.of(2022, 9, 24);
        LocalDate endDate = LocalDate.of(2023, 5, 28);

        startDate.datesUntil(endDate.plusDays(1)).forEach(date -> createShiftIfNeeded(date));

        //        shiften.stream().forEach(System.out::println);
        //        tappers.stream().forEach(System.out::println);
        writeShiftenToCSV();
        writePerTapperToCSV();
    }

    private static void createShiftIfNeeded(LocalDate date) {
        Collections.shuffle(tappers); // for some randomness

        if (date.isAfter(startWinterStop) && date.isBefore(endWinterStop)) {
            return; // No shifts during winterstop
        }

        switch (date.getDayOfWeek()) {
        case MONDAY -> shiften.add(createShiftForPloeg(date, Ploeg.VROUWEN));
        case TUESDAY -> shiften.add(createShiftForPloeg(date, Ploeg.MANNEN));
        case THURSDAY -> {
            if (date.isBefore(LocalDate.of(2022, 10, 14))) {
                shiften.add(createJeugdShift(date));
            }
            shiften.add(createShiftForAllPloegen(date));
        }
        case SATURDAY, SUNDAY -> {
            var match = dateHasHomeMatch(date);
            if (match.isPresent()) {
                shiften.add(createMatchShift(date, getOtherPloeg(match.get().getValue())));
            }
        }
        case WEDNESDAY, FRIDAY -> {
        }
        default -> throw new IllegalStateException("Unexpected value: " + date.getDayOfWeek());
        }

    }

    private static Ploeg getOtherPloeg(Ploeg ploeg) {
        return ploeg.equals(Ploeg.VROUWEN) ? Ploeg.MANNEN : Ploeg.VROUWEN;
    }

    private static Shift createShiftForPloeg(LocalDate date, Ploeg ploeg) {
        return new Shift(date.atTime(19, 30), LocalTime.of(00, 00),
            date.getDayOfWeek(),
            List.of(findTapperForShift(ploeg), findTapperForShift(ploeg)),
            "Training " + ploeg.toString().toLowerCase());
    }

    private static Shift createJeugdShift(LocalDate date) {
        return new Shift(date.atTime(18, 0), LocalTime.of(19, 30),
            date.getDayOfWeek(),
            List.of(findTapperForShift(Ploeg.MANNEN), findTapperForShiftMatch(Ploeg.VROUWEN)),
            "Training jeugd");
    }

    //TODO 1 man en 1 vrouw? of zorgen dat er per persoon evenveel shiften zijn?? ==> findTapperForShiftNoPloeg gebruiken dan.
    private static Shift createShiftForAllPloegen(LocalDate date) {
        return new Shift(date.atTime(19, 30), LocalTime.of(00, 00),
            date.getDayOfWeek(),
            List.of(findTapperForShift(Ploeg.VROUWEN), findTapperForShift(Ploeg.MANNEN)),
            "Training " + Ploeg.MANNEN.toString().toLowerCase() + "/" + Ploeg.VROUWEN.toString().toLowerCase());
    }

    private static Shift createMatchShift(LocalDate date, Ploeg ploeg) {
        return new Shift(date.atTime(13, 30), LocalTime.of(20, 00),
            date.getDayOfWeek(),
            IntStream.range(0, 6)
                .mapToObj(i -> findTapperForShiftMatch(ploeg))
                .collect(Collectors.toList()),
            "Match " + getOtherPloeg(ploeg).toString().toLowerCase());
    }

    private static Tapper findTapperForShift(Ploeg ploeg) {
        int leastAmountOfShifts = tappers.stream()
            .filter(t -> t.getPloeg().equals(ploeg))
            .map(Tapper::getAantalTrainingen)
            .mapToInt(i -> i)
            .min()
            .getAsInt();
        Tapper tapper = tappers.stream()
            .filter(t -> t.getAantalTrainingen() == leastAmountOfShifts)
            .filter(t -> t.getPloeg().equals(ploeg))
            .findAny()
            .get();
        tapper.addTraining();
        return tapper;
    }

    private static Tapper findTapperForShiftNoPloeg() {
        int leastAmountOfShifts = tappers.stream()
            .map(Tapper::getAantalTrainingen)
            .mapToInt(i -> i)
            .min()
            .getAsInt();
        Tapper tapper = tappers.stream()
            .filter(t -> t.getAantalTrainingen() == leastAmountOfShifts)
            .findAny()
            .get();
        tapper.addTraining();
        return tapper;
    }

    private static Tapper findTapperForShiftMatch(Ploeg ploeg) {
        int leastAmountOfShifts = tappers.stream()
            .filter(t -> t.getPloeg().equals(ploeg))
            .map(tapper -> tapper.getAantalMatchen())
            .mapToInt(i -> i)
            .min()
            .getAsInt();
        Tapper tapper = tappers.stream()
            .filter(t -> t.getAantalMatchen() == leastAmountOfShifts)
            .filter(t -> t.getPloeg().equals(ploeg))
            .findAny()
            .get();
        tapper.addMatch();
        return tapper;
    }

    private static Optional<Entry<LocalDate, Ploeg>> dateHasHomeMatch(LocalDate date) {
        return matchen.entrySet().stream().filter(entry -> entry.getKey().isEqual(date))
            .findFirst();
    }

    private static void writeShiftenToCSV() {
        List<String[]> lines = shiften.stream()
            .map(Shift::toArray)
            .collect(Collectors.toList());
        try {
            Path path = Paths.get(ClassLoader.getSystemResource("tappers.csv").toURI());
            try (CSVWriter writer = new CSVWriter(new FileWriter(path.toString()))) {
                lines.forEach(writer::writeNext);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writePerTapperToCSV() {
        List<String[]> tapperLines = new ArrayList<>();
        tappers.stream().sorted().forEach(tapper -> {
            List<Shift> listForTapper = shiften.stream()
                .filter(shift -> shift.tappers().contains(tapper))
                .collect(Collectors.toList());
            tapperLines.add(tapper.createCVSLine(listForTapper));
        });
        try {
            Path path = Paths.get(ClassLoader.getSystemResource("perPersoon.csv").toURI());
            try (CSVWriter writer = new CSVWriter(new FileWriter(path.toString()))) {
                tapperLines.forEach(writer::writeNext);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static void vulLedenLijst() {
        tappers = new ArrayList<>();
        tappers.add(new Tapper("Anthuenis Els", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("Baetens Elien", Ploeg.VROUWEN, 1, 0));
        tappers.add(new Tapper("Bosman Kaat", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("Bosman Lieselotte", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("Buyle Hannelore", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("Cooreman Ludiwien", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("Dauwe Irjen", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("De Beir Jasmine", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("De Bondt Silke", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("De Brabander Jana", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("De Brabander Jitske", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("De Cock Lyana", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("De Decker Damiet", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("De Graef Kim", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("De Troyer Katrijn", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("De Vuyst Lynn", Ploeg.VROUWEN, 1, 0));
        tappers.add(new Tapper("Dewitte Daphné", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("Dierickx Evelyne", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("Duerinck Manon", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("Dumez Charlotte", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("Haentjes Lisa", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("Moerman Britt", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("Reper Charo", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("Scholliers Jana", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("Thibau Anke", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("Van Driessche Ashley", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("Van Driessche Irmgard", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("Van Malderen Emma", Ploeg.VROUWEN, 1, 0));
        tappers.add(new Tapper("Van Puyvelde Anke", Ploeg.VROUWEN, 1, 0));
        tappers.add(new Tapper("Van Zande Ilona", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("Veyt Lieve", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("Veyt Nele", Ploeg.VROUWEN, 0, 0));
        tappers.add(new Tapper("Withofs Valerie", Ploeg.VROUWEN, 0, 0));

        tappers.add(new Tapper("Boone Sven", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Callebaut Anton", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Celi Jarne", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Christiaens Wout", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Criel Dajo", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("D'hont Kjell", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("D'hont Nils", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("De Bie Hans", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("De Bruyne Lieven", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("De Coster Bram", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("De Cuyper Sam", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("De Graeve Yenthel", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("De Groot Timothy", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("De Maesschalck Bram", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("De Saedelaere Simon", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("De Troyer Lucas", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("De Vreese Jacob", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Everaert Frederik", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Govaert Arno", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Heirman Kristof", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Huygens Jeroen", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Kerre Davy", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Klein Joni", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Lanckbeen Tom", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Matthijs Piet", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Moerman Geert", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Pieters Jonathan", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Raemdonck Preben", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Rijckbosch Ebbe", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Roelandt Tom", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Rottiers Sam", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Scrivens Jason", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Spriet Juul", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Thibau Davy", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Van De Voorde Filip", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Van Hauwermeiren Koen", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Van Puyvelde Ben", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Van Puyvelde Stef", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Vermeir Aaron", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Vermonden Joeri", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Waegeman Yorick", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Withofs Wouter", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Zaman Alexander", Ploeg.MANNEN, 0, 0));
        Collections.shuffle(tappers);
    }

    private static void vulMatchen() {
        matchen = new HashMap<>();
        matchen.put(LocalDate.of(2022, 11, 2), Ploeg.MANNEN);
        matchen.put(LocalDate.of(2022, 11, 27), Ploeg.MANNEN);
        matchen.put(LocalDate.of(2023, 1, 29), Ploeg.MANNEN);
        matchen.put(LocalDate.of(2023, 2, 12), Ploeg.MANNEN);
        matchen.put(LocalDate.of(2023, 3, 12), Ploeg.MANNEN);
        matchen.put(LocalDate.of(2023, 3, 26), Ploeg.MANNEN);
        matchen.put(LocalDate.of(2023, 4, 23), Ploeg.MANNEN);

        //        matchen.put(LocalDate.of(2022, 9, 24), Ploeg.VROUWEN);
        matchen.put(LocalDate.of(2022, 10, 8), Ploeg.VROUWEN);
        matchen.put(LocalDate.of(2022, 11, 5), Ploeg.VROUWEN);
        matchen.put(LocalDate.of(2022, 11, 12), Ploeg.VROUWEN);
        matchen.put(LocalDate.of(2022, 12, 3), Ploeg.VROUWEN);
        matchen.put(LocalDate.of(2023, 1, 21), Ploeg.VROUWEN);
        matchen.put(LocalDate.of(2023, 2, 4), Ploeg.VROUWEN);
        matchen.put(LocalDate.of(2023, 3, 11), Ploeg.VROUWEN);
        matchen.put(LocalDate.of(2023, 4, 8), Ploeg.VROUWEN);
    }
}
