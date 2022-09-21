import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {

    private static List<Shift> shiften = new ArrayList<>();
    private static List<Tapper> tappers;
    private static Map<LocalDate, Ploeg> matchen;

    public static void main(String[] args) {

        vulLedenLijst();
        System.out.println("Aantal tappers: " + tappers.size());
        vulMatchen();
        System.out.println("Aantal matchen: " + matchen.size());

        LocalDate startDate = LocalDate.of(2022, 9, 26);
        LocalDate endDate = LocalDate.of(2023, 5, 28);

        startDate.datesUntil(endDate.plusDays(1)).forEach(date -> createShiftIfNeeded(date));

        shiften.stream().forEach(System.out::println);
    }

    private static void createShiftIfNeeded(LocalDate date) {
        Collections.shuffle(tappers); // for some randomness
        switch (date.getDayOfWeek()) {
            case MONDAY -> shiften.add(createShiftForPloeg(date, Ploeg.VROUWEN));
            case TUESDAY -> shiften.add(createShiftForPloeg(date, Ploeg.MANNEN));
            case THURSDAY -> shiften.add(createShiftForAllPloegen(date));
            case SATURDAY, SUNDAY -> {
                var match = dateHasHomeMatch(date);
                if (match.isPresent()) {
                    shiften.add(createMatchShift(date, match.get().getValue()));
                }
            }
            case WEDNESDAY, FRIDAY -> {
            }
            default -> throw new IllegalStateException("Unexpected value: " + date.getDayOfWeek());
        }

    }

    private static Shift createShiftForPloeg(LocalDate date, Ploeg ploeg) {
        return new Shift(date.atTime(19, 30), date.getDayOfWeek(),
            List.of(findTapperForShift(ploeg), findTapperForShift(ploeg)));
    }

    private static Shift createShiftForAllPloegen(LocalDate date) {
        return new Shift(date.atTime(19, 30), date.getDayOfWeek(),
            List.of(findTapperForShift(Ploeg.VROUWEN), findTapperForShift(Ploeg.MANNEN)));
    }

    private static Shift createMatchShift(LocalDate date, Ploeg ploeg) {
        return new Shift(date.atTime(13, 30), date.getDayOfWeek(),
            IntStream.range(0, 6)
                .mapToObj(i -> findTapperForShiftMatch(ploeg))
                .collect(Collectors.toList()));
    }

    private static Tapper findTapperForShift(Ploeg ploeg) { //TODO magic
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

    private static Tapper findTapperForShiftMatch(Ploeg ploeg) { //TODO magic
        int leastAmountOfShifts = tappers.stream()
            .map(tapper -> tapper.getAantalMatchen())
            .mapToInt(i -> i)
            .min()
            .getAsInt();
        Tapper tapper = tappers.stream()
            .filter(t -> t.getAantalMatchen() == leastAmountOfShifts)
            .findAny()
            .get();
        tapper.addMatch();
        return tapper;
    }

    private static Optional<Entry<LocalDate, Ploeg>> dateHasHomeMatch(LocalDate date) {
        return matchen.entrySet().stream().filter(entry -> entry.getKey().isEqual(date))
            .findFirst();
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
        tappers.add(new Tapper("De Clerck Stefan", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("De Coster Bram", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("De Cuyper Sam", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("De Graeve Yenthel", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("De Groot Timothy", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("De Maesschalck Bram", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("De Saedelaere Simon", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("De Troyer Lucas", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("De Vreese Jacob", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Devits Mats", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Everaert Frederik", Ploeg.MANNEN, 0, 0));
        tappers.add(new Tapper("Fory Yoran", Ploeg.MANNEN, 0, 0));
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
