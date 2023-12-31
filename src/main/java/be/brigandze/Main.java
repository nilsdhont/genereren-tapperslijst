package be.brigandze;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static be.brigandze.Ploeg.*;
import static java.util.stream.Collectors.toList;

public class Main {

  private static List<Shift> shiften = new ArrayList<>();
  private static List<Tapper> tappers;
  private static Map<LocalDate, Ploeg> matchen;
  private static Map<LocalDate, Ploeg> matchenJeugd;

  // TODO Periodes voor schoolverloven

  private static LocalDate startSeizoen = LocalDate.of(2024, 01, 4);
  private static LocalDate startWinterStop = LocalDate.of(2023, 12, 22);
  private static LocalDate endWinterStop = LocalDate.of(2024, 1, 3);
  private static LocalDate eindSeizoen = LocalDate.of(2024, 5, 19);

  //    private static LocalDate firstDateZaterdagJeugdShiftTweeWekelijks = LocalDate.of(2022, 9,
  // 24);

  public static void main(String[] args) {

    tappers = TappersManager.readTappersFromCSV();
    shiften = Shiften.readExistingFromCSV(tappers);
    vulMatchen();

    startSeizoen.datesUntil(eindSeizoen).forEach(date -> createShiftIfNeeded(date));

    shiften.stream().forEach(System.out::println);
    tappers.stream().forEach(System.out::println);
    writeShiftenToCSV();
    writePerTapperToCSV();
  }

  private static void createShiftIfNeeded(LocalDate date) {
    Collections.shuffle(tappers); // for some randomness

    if (date.isAfter(startWinterStop) && date.isBefore(endWinterStop)) {
      return; // No shifts during winterstop
    }

    switch (date.getDayOfWeek()) {
      case MONDAY -> shiften.add(
          createShiftForTrainingMaandagDinsdag(date, VROUWEN)); // Training vrouwen
      case TUESDAY -> {
        shiften.add(createJeugdShiftWeekdag(date, false));
        shiften.add(createShiftForTrainingMaandagDinsdag(date, MANNEN)); // Training mannen
      }
      case THURSDAY -> {
        shiften.add(createJeugdShiftWeekdag(date, true));
        shiften.add(createShiftForTrainingDonderdag(date)); // Training mannen/vrouwen7

      }
      case SATURDAY -> {
        shiften.add(createJeugdShiftZaterdag(date));
        createMatchShift(date); // Vrouwen matchen
        createMatchShiftJeugd(date); // Jeugd matchen
      }
      case SUNDAY -> createMatchShift(date); // Mannen matchen
      case WEDNESDAY,
          FRIDAY -> {} // Niks op woensdag. Vrijdag doet touch zelf zijn toog. kunnen evt
        // placeholders voor gemaakt worden?
    }
  }

  private static void createMatchShift(LocalDate date) {
    var match = dateHasHomeMatch(date);
    if (match.isPresent()) {
      shiften.add(createMatchShift(date, getOtherPloeg(match.get().getValue())));
    }
  }

  private static void createMatchShiftJeugd(LocalDate date) {
    matchenJeugd.entrySet().stream()
        .filter(entry -> entry.getKey().isEqual(date))
        .forEach(entry -> shiften.add(createMatchShiftJeugd2Tappers(entry)));
  }

  private static Ploeg getOtherPloeg(Ploeg ploeg) {
    return ploeg.equals(VROUWEN) ? MANNEN : VROUWEN;
  }

  private static Shift createShiftForTrainingMaandagDinsdag(LocalDate date, Ploeg ploeg) {
    return new Shift(
        date.atTime(19, 30),
        LocalTime.of(00, 00),
        date.getDayOfWeek(),
        List.of(
            findTapperForTrainingMaandagDinsdag(ploeg), findTapperForTrainingMaandagDinsdag(ploeg)),
        "Training " + ploeg.toString().toLowerCase());
  }

  private static Shift createJeugdShiftWeekdag(LocalDate date, boolean tappers) {
    return new Shift(
        date.atTime(18, 0),
        LocalTime.of(19, 30),
        date.getDayOfWeek(),
        tappers ? List.of(findTapperForTrainingJeugd(), findTapperForTrainingJeugd()) : List.of(),
        "Training jeugd");
  }

  private static Shift createJeugdShiftZaterdag(LocalDate date) {
    return new Shift(
        date.atTime(9, 45), LocalTime.of(12, 30), date.getDayOfWeek(), List.of(), "Training jeugd");
  }

  private static Shift createShiftForTrainingDonderdag(LocalDate date) {
    return new Shift(
        date.atTime(19, 30),
        LocalTime.of(00, 00),
        date.getDayOfWeek(),
        List.of(findTapperForTrainingDonderdag(), findTapperForTrainingDonderdag()),
        "Training " + MANNEN.toString().toLowerCase() + "/" + VROUWEN.toString().toLowerCase());
  }

  private static Shift createMatchShift(LocalDate date, Ploeg ploeg) {
    List<Tapper> seniorenTappers =
        IntStream.range(0, 4).mapToObj(i -> findTapperForMatch(ploeg)).collect(toList());
    List<Tapper> touchTappers =
        IntStream.range(0, 2).mapToObj(i -> findTapperForMatch(TOUCH)).collect(toList());
    return new Shift(
        date.atTime(13, 30),
        LocalTime.of(20, 00),
        date.getDayOfWeek(),
        Stream.concat(seniorenTappers.stream(), touchTappers.stream()).collect(toList()),
        "Match " + getOtherPloeg(ploeg).toString().toLowerCase());
  }

  private static Shift createMatchShiftJeugd2Tappers(Entry<LocalDate, Ploeg> match) {
    LocalTime time = LocalTime.of(12, 0);
    switch (match.getValue()) {
      case U14 -> time = LocalTime.of(12, 0);
      case U16 -> time = LocalTime.of(13, 30);
      case U18 -> time = LocalTime.of(15, 0);
    }

    return new Shift(
        match.getKey().atTime(time),
        time.plusHours(2),
        match.getKey().getDayOfWeek(),
        List.of(findTapperForTrainingJeugd(), findTapperForTrainingJeugd()),
        "Match Jeugd");
  }

  private static Tapper findTapperForTrainingMaandagDinsdag(Ploeg ploeg) {
    int leastAmountOfTrainingen =
        tappers.stream()
            .filter(t -> t.getPloeg().equals(ploeg))
            .map(Tapper::getAantalTrainingenMaandagDinsdag)
            .mapToInt(i -> i)
            .min()
            .getAsInt();
    List<Tapper> candidates =
        tappers.stream()
            .filter(t -> t.getAantalTrainingenMaandagDinsdag() == leastAmountOfTrainingen)
            .filter(t -> t.getPloeg().equals(ploeg))
            .collect(toList());
    int leastAmountOfMatchen =
        candidates.stream().map(Tapper::getAantalMatchen).mapToInt(i -> i).min().getAsInt();
    Collections.shuffle(candidates);
    Tapper tapper =
        candidates.stream()
            .filter(t -> t.getAantalMatchen() == leastAmountOfMatchen)
            .findAny()
            .get();
    tapper.addTrainingMaandagDinsdag();
    return tapper;
  }

  private static Tapper findTapperForTrainingJeugd() {
    int leastAmountOfTrainingen =
        tappers.stream()
            .filter(t -> !t.isTrainer())
            .filter(t -> t.getPloeg().isTaptJeugd())
            .map(Tapper::getAantalTrainingenJeugd)
            .mapToInt(i -> i)
            .min()
            .getAsInt();
    List<Tapper> candidates =
        tappers.stream()
            .filter(t -> !t.isTrainer())
            .filter(t -> t.getPloeg().isTaptJeugd())
            .filter(t -> t.getAantalTrainingenJeugd() == leastAmountOfTrainingen)
            .collect(toList());
    int leastAmountOfMatchen =
        candidates.stream().map(Tapper::getAantalMatchen).mapToInt(i -> i).min().getAsInt();
    Collections.shuffle(candidates);
    Tapper tapper =
        candidates.stream()
            .filter(t -> t.getAantalMatchen() == leastAmountOfMatchen)
            .findAny()
            .get();
    tapper.addTrainingJeugd();
    return tapper;
  }

  private static Tapper findTapperForTrainingDonderdag() {
    int leastAmountOfTrainingen =
        tappers.stream()
            .filter(tapper -> tapper.getPloeg().isTaptSeniorenTraining())
            .filter(tapper -> !tapper.isTrainer())
            .map(Tapper::getAantalTrainingenDonderdag)
            .mapToInt(i -> i)
            .min()
            .getAsInt();
    List<Tapper> candidates =
        tappers.stream()
            .filter(tapper -> tapper.getPloeg().isTaptSeniorenTraining())
            .filter(tapper -> !tapper.isTrainer())
            .filter(t -> t.getAantalTrainingenDonderdag() == leastAmountOfTrainingen)
            .collect(toList());
    int leastAmountOfMatchen =
        candidates.stream().map(Tapper::getAantalMatchen).mapToInt(i -> i).min().getAsInt();

    Collections.shuffle(candidates);
    Tapper tapper =
        candidates.stream()
            .filter(t -> t.getAantalMatchen() == leastAmountOfMatchen)
            .findAny()
            .get();
    tapper.addTrainingDonderdag();
    return tapper;
  }

  private static Tapper findTapperForMatch(Ploeg ploeg) {
    int leastAmountOfMatchen =
        tappers.stream()
            .filter(t -> t.getPloeg().equals(ploeg))
            .map(Tapper::getAantalMatchen)
            .mapToInt(i -> i)
            .min()
            .getAsInt();
    List<Tapper> candidates =
        tappers.stream()
            .filter(t -> t.getAantalMatchen() == leastAmountOfMatchen)
            .filter(t -> t.getPloeg().equals(ploeg))
            .collect(toList());
    //    int leastAmountOfTrainingen =
    //        candidates.stream()
    //            .map(Tapper::getAantalTrainingenDonderdag)
    //            .mapToInt(i -> i)
    //            .min()
    //            .getAsInt();
    Collections.shuffle(candidates);
    Tapper tapper =
        candidates.stream()
            //            .filter(t -> t.getAantalTrainingenDonderdag() == leastAmountOfTrainingen)
            .findAny()
            .get();
    tapper.addMatch();
    return tapper;
  }

  private static Optional<Entry<LocalDate, Ploeg>> dateHasHomeMatch(LocalDate date) {
    return matchen.entrySet().stream().filter(entry -> entry.getKey().isEqual(date)).findFirst();
  }

  private static Optional<Entry<LocalDate, Ploeg>> dateHasHomeMatchJeugd(LocalDate date) {
    return matchenJeugd.entrySet().stream()
        .filter(entry -> entry.getKey().isEqual(date))
        .findFirst();
  }

  private static void writeShiftenToCSV() {
    List<String[]> lines = shiften.stream().map(Shift::toArray).collect(toList());
    try {
      Path path = Paths.get(ClassLoader.getSystemResource("tapperslijst.csv").toURI());
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
    tappers.stream()
        .sorted()
        .forEach(
            tapper -> {
              List<Shift> listForTapper =
                  shiften.stream()
                      .filter(shift -> shift.tappers().contains(tapper))
                      .collect(toList());
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
    //        tappers = new ArrayList<>();
    //        tappers.add(new Tapper("Anthuenis Els", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Baetens Elien", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Bosman Kaat", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Bosman Lieselotte", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Buyle Hannelore", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Cooreman Ludiwien", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Dauwe Irjen", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("De Beir Jasmine", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("De Bondt Silke", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("De Brabander Jana", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("De Brabander Jitske", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("De Cock Lyana", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("De Decker Damiet", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("De Graef Kim", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("De Troyer Katrijn", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("De Vuyst Lynn", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Dewitte Daphné", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Dierickx Evelyne", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Duerinck Manon", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Dumez Charlotte", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Haentjes Lisa", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Moerman Britt", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Raes Liesbeth", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Reper Charo", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Ruymaekers Lynn", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Schatteman Brenthe", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Scholliers Jana", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Steyaert Tini", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Thibau Anke", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Van Driessche Ashley", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Van Driessche Irmgard", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Van Malderen Emma", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Van Puyvelde Anke", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Van Zande Ilona", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Veyt Lieve", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Veyt Nele", VROUWEN, false, 0, 0));
    //        tappers.add(new Tapper("Withofs Valerie", VROUWEN, false, 0, 0));

    // Mannen
    //        tappers.add(new Tapper("Boone Sven", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Callebaut Anton", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Celi Jarne", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Christiaens Wout", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Criel Dajo", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("D'hont Kjell", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("D'hont Nils", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("De Bie Hans", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("De Bruyne Lieven", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("De Coster Bram", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("De Cuyper Sam", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("De Graeve Yenthel", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("De Groot Timothy", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("De Maesschalck Bram", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("De Saedelaere Simon", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Wouter De Witte", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("De Troyer Lucas", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("De Vreese Jacob", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Eeckeleers Arne", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Everaert Frederik", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Govaert Arno", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Heirman Kristof", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Huygens Jeroen", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Kerre Davy", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Klein Joni", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Lanckbeen Tom", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Luinstra Tuur", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Matthijs Piet", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Moerman Geert", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Pieters Jonathan", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Raemdonck Preben", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Rijckbosch Ebbe", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Roelandt Tom", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Rottiers Sam", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Schatteman-Bracke Elias", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Scrivens Jason", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Spriet Juul", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Thibau Davy", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Van De Voorde Filip", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Van Hauwermeiren Koen", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Van Puyvelde Ben", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Van Puyvelde Stef", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Verbruggen Peter-Paul", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Vermeir Aaron", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Vermonden Joeri", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Waegeman Yorick", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Withofs Wouter", MANNEN, false, 0, 0));
    //        tappers.add(new Tapper("Zaman Alexander", MANNEN, false, 0, 0));
    //        Collections.shuffle(tappers);
  }

  private static void vulMatchen() {
    matchen = new HashMap<>();
    matchen.put(LocalDate.of(2023, 10, 1), MANNEN); // Laakdal
    matchen.put(LocalDate.of(2023, 10, 22), MANNEN); // Mechelen
    matchen.put(LocalDate.of(2023, 11, 12), MANNEN); // Waereghem
    matchen.put(LocalDate.of(2023, 12, 3), MANNEN); // DRC3
    matchen.put(LocalDate.of(2023, 12, 10), MANNEN); // Arendonk
    matchen.put(LocalDate.of(2024, 1, 28), MANNEN); // Curtrycke
    matchen.put(LocalDate.of(2024, 2, 11), MANNEN); // DRC3
    matchen.put(LocalDate.of(2024, 2, 25), MANNEN); // Mechelen
    matchen.put(LocalDate.of(2024, 3, 17), MANNEN); // Beernem

    matchen.put(LocalDate.of(2023, 9, 16), VROUWEN); // Oemoemenoe
    matchen.put(LocalDate.of(2023, 9, 23), VROUWEN); // Pajot
    matchen.put(LocalDate.of(2023, 10, 21), VROUWEN); // Mechelen

    matchen.put(LocalDate.of(2024, 01, 13), VROUWEN); // Entente Kortrijk/dmd/leuven
    matchen.put(LocalDate.of(2024, 01, 27), VROUWEN); // ottignies

    matchenJeugd = new HashMap<>();
    matchenJeugd.put(LocalDate.of(2024, 11, 11), U10);

    matchenJeugd.put(LocalDate.of(2023, 9, 9), U14);
    matchenJeugd.put(LocalDate.of(2023, 9, 30), U14);
    matchenJeugd.put(LocalDate.of(2023, 10, 14), U14);
    matchenJeugd.put(LocalDate.of(2023, 11, 11), U14);

    matchenJeugd.put(LocalDate.of(2024, 04, 13), U14);

    matchenJeugd.put(LocalDate.of(2023, 9, 9), U16);
    matchenJeugd.put(LocalDate.of(2023, 10, 7), U16);

    matchenJeugd.put(LocalDate.of(2024, 01, 20), U16);
    matchenJeugd.put(LocalDate.of(2024, 04, 20), U16);

    matchenJeugd.put(LocalDate.of(2023, 9, 9), U18);
    matchenJeugd.put(LocalDate.of(2023, 9, 30), U18);
    matchenJeugd.put(LocalDate.of(2023, 10, 21), U18);
    matchenJeugd.put(LocalDate.of(2023, 11, 18), U18);

    //        matchenJeugd.put(LocalDate.of(2023, 11, 5), U15_U18_POWER_GIRLS);
  }
}
