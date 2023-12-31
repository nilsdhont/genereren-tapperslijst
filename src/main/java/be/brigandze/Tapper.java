package be.brigandze;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static be.brigandze.Shift.vertaalDayOfWeek;

public class Tapper implements Comparable<Tapper>{

    private String naam;

    private Ploeg ploeg;
    private boolean isTrainer;
    private int aantalMatchen;
    private int aantalTrainingenMaandagDinsdag;
    private int aantalTrainingenDonderdag;

    @Override
    public String toString() {
        return "Tapper{" +
                "naam='" + naam + '\'' +
                ", ploeg=" + ploeg +
                ", isTrainer=" + isTrainer +
                ", aantalMatchen=" + aantalMatchen +
                ", aantalTrainingenMaandagDinsdag=" + aantalTrainingenMaandagDinsdag +
                ", aantalTrainingenDonderdag=" + aantalTrainingenDonderdag +
                ", aantalTrainingenJeugd=" + aantalTrainingenJeugd +
                '}';
    }

    private int aantalTrainingenJeugd;

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public Ploeg getPloeg() {
        return ploeg;
    }

    public void setPloeg(Ploeg ploeg) {
        this.ploeg = ploeg;
    }

    public boolean isTrainer() {
        return isTrainer;
    }

    public void setTrainer(boolean trainer) {
        isTrainer = trainer;
    }

    public int getAantalMatchen() {
        return aantalMatchen;
    }


    public int getAantalTrainingenMaandagDinsdag() {
        return aantalTrainingenMaandagDinsdag;
    }


    public int getAantalTrainingenDonderdag() {
        return aantalTrainingenDonderdag;
    }


    public int getAantalTrainingenJeugd() {
        return aantalTrainingenJeugd;
    }


    public void addTrainingDonderdag(){
        aantalTrainingenDonderdag++;
    }

    public void addTrainingMaandagDinsdag(){
        aantalTrainingenMaandagDinsdag++;
    }

    public void addTrainingJeugd(){
        aantalTrainingenJeugd++;
    }
    public void addMatch(){
        aantalMatchen++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Tapper tapper = (Tapper) o;
        return naam.equals(tapper.naam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(naam);
    }

    public String[] createCVSLine(List<Shift> listForTapper) {
        List<String> list = new ArrayList<>();
        list.add(naam);
        list.add(String.valueOf(aantalTrainingenMaandagDinsdag));
        list.add(String.valueOf(aantalTrainingenDonderdag));
        list.add(String.valueOf(aantalTrainingenJeugd));
        list.add(String.valueOf(aantalMatchen));
        listForTapper.forEach(shift -> {
            StringBuilder s = new StringBuilder();
            s.append(vertaalDayOfWeek(shift.startDateTime().getDayOfWeek()));
            s.append(" ");
            s.append(shift.startDateTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            s.append(" ");
            s.append(shift.beschrijving());
            list.add(s.toString());
        });
        return list.toArray(new String[0]);
    }

    @Override
    public int compareTo(Tapper t) {
        return this.naam.compareTo(t.naam);
    }

    public void addShift(SoortShift soortShift) {
        switch (soortShift){
            case TRAINING_JEUGD -> aantalTrainingenJeugd++;
            case TRAINING_MAANDAG, TRAINING_DINSDAG -> aantalTrainingenMaandagDinsdag++;
            case TRAINING_DONDERDAG -> aantalTrainingenDonderdag++;
            case MATCH_JEUGD, MATCH_SENIOREN_VROUWEN, MATCH_SENIOREN_MANNEN -> aantalMatchen++;
        }
    }
}
